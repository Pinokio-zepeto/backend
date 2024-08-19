package com.example.pinokkio.api.order;

import com.example.pinokkio.api.customer.Customer;
import com.example.pinokkio.api.customer.CustomerRepository;
import com.example.pinokkio.api.item.Item;
import com.example.pinokkio.api.item.ItemRepository;
import com.example.pinokkio.api.kiosk.Kiosk;
import com.example.pinokkio.api.order.dto.request.GroupOrderItemRequest;
import com.example.pinokkio.api.order.dto.request.OrderDurationRequest;
import com.example.pinokkio.api.order.dto.request.OrderItemRequest;
import com.example.pinokkio.api.order.dto.response.OrderDetailResponse;
import com.example.pinokkio.api.order.dto.response.OrderItemDetail;
import com.example.pinokkio.api.order.dto.response.TopOrderedItemResponse;
import com.example.pinokkio.api.order.orderitem.OrderItem;
import com.example.pinokkio.api.order.orderitem.OrderItemRepository;
import com.example.pinokkio.api.order.statistics.SalesStatisticsService;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.pos.dto.response.PosStatisticsResponse;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.common.type.OrderStatus;
import com.example.pinokkio.config.RedisUtil;
import com.example.pinokkio.exception.domain.customer.CustomerNotFoundException;
import com.example.pinokkio.exception.domain.customer.NotCustomerOfPosException;
import com.example.pinokkio.exception.domain.item.ItemAmountException;
import com.example.pinokkio.exception.domain.item.ItemNotFoundException;
import com.example.pinokkio.exception.domain.order.OrderNotFoundException;
import com.example.pinokkio.exception.domain.pos.PosNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final PosRepository posRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final RedisUtil redisUtil;
    private final UserService userService;
    private final SalesStatisticsService salesStatisticsService;

    /**
     * 주문 요청정보를 기반으로 주문을 생성한다.
     * @param dtoList   주문 요청정보
     * @return 생성된 주문 정보
     */
    public Order createOrder(GroupOrderItemRequest dtoList) {
        // Pos 검증
        Kiosk currentKiosk = userService.getCurrentKiosk();
        Pos pos = currentKiosk.getPos();

        UUID customerId = dtoList.getCustomerId() == null
                ? pos.getDummyCustomerUUID()
                : dtoList.getCustomerId();

        validateCustomer(customerId, pos.getId());

        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // OrderItem 리스트 생성
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest request : dtoList.getOrderItems()) {
            Item item = itemRepository
                    .findById(request.getItemId())
                    .orElseThrow(() -> new ItemNotFoundException(request.getItemId()));

            // Item 수량 체크
            if (item.getAmount() < request.getQuantity()) {
                throw new ItemAmountException(item.getId());
            }

            // Item 수량 차감 + OrderItem 생성
            item.updateAmount(-request.getQuantity());
            OrderItem orderItem = new OrderItem(null, item, customerId, request.getQuantity());
            orderItems.add(orderItem);
        }

        // Order 생성
        long totalPrice = calculateTotalPrice(orderItems);
        updateSalesInRedis(pos.getId(), totalPrice);

        Order order = Order.builder()
                .pos(pos)
                .customer(customer)
                .items(orderItems)
                .totalPrice(totalPrice)
                .build();

        // OrderItem 의 Order 설정 및 저장
        orderItems.forEach(orderItem -> {
            orderItem.updateOrder(order);
            orderItemRepository.save(orderItem);
        });

        // Order 저장
        Order savedOrder = orderRepository.save(order);
        salesStatisticsService.updateSalesStatisticsOnOrderChange(order, order.getTotalPrice());
        return savedOrder;
    }

    /**
     * 고객 식별자를 기반으로 가장 많이 주문한 아이템 정보를 반환한다.
     * @param customerId 고객 식별자
     * @return 가장 많이 주문된 아이템 정보를 담은 TopOrderedItemResponse
     */
    public Optional<TopOrderedItemResponse> getTopOrderedItemByCustomerId(UUID customerId) {
        //검증
        Kiosk currentKiosk = userService.getCurrentKiosk();
        UUID posId = currentKiosk.getPos().getId();
        validateCustomer(customerId, posId);
        //리스트 생성
        List<Object[]> results = orderItemRepository.findTopOrderedItemsByCustomerId(customerId);
        if (!results.isEmpty()) {
            Object[] topItem = results.getFirst();
            Item item = (Item) topItem[0];
            int totalQuantity = ((Number) topItem[1]).intValue();
            return Optional.of(new TopOrderedItemResponse(
                    item.getId(),
                    item.getName(),
                    totalQuantity)
            );
        }
        //주문한 아이템이 없을 경우
        return Optional.empty();
    }

    /**
     * 입력받은 고객이 입력받은 포스의 고객이 맞는지 확인한다.
     * @param customerId 고객 식별자
     * @param posId      포스 식별자
     */
    public void validateCustomer(UUID customerId, UUID posId) {
        Pos pos = posRepository
                .findById(posId)
                .orElseThrow(() -> new PosNotFoundException(posId));
        Customer customer = customerRepository
                .findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        if (!customer.getId().equals(pos.getDummyCustomerUUID()))
            throw new NotCustomerOfPosException(customerId);
    }

    /**
     * 특정 고객의 최근 주문에서 아이템 리스트를 반환한다.
     * @param customerId 고객 식별자
     * @return 최근 주문의 아이템 리스트
     */
    public List<OrderItem> getRecentOrderItemsByCustomerId(UUID customerId) {
        Kiosk currentKiosk = userService.getCurrentKiosk();
        UUID posId = currentKiosk.getPos().getId();

        validateCustomer(customerId, posId);
        return orderRepository.findByCustomerIdOrderByCreatedDateDesc(customerId)
                .stream()
                .findFirst()
                .map(Order::getItems)
                .orElseGet(ArrayList::new);
    }

    /**
     * 특정 주문의 상태를 전환한다. [완료, 취소]
     * @param orderId 주문 식별자
     */
    public void toggleOrderStatus(UUID orderId) {
        Pos currentPos = userService.getCurrentPos();

        Order findOrder = orderRepository
                .findByPosIdAndId(currentPos.getId(), orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        findOrder.toggleOrderStatus();
        if (findOrder.getStatus() == OrderStatus.CANCELLED) {
            salesStatisticsService.updateSalesStatisticsOnOrderChange(findOrder, -findOrder.getTotalPrice());
        }
    }

    /**
     * OrderItem 리스트의 합산 값을 반환한다.
     * @param items OrderItem 리스트
     * @return 합산 값
     */
    private long calculateTotalPrice(List<OrderItem> items) {
        return items.stream()
                .mapToLong(item -> (long) item.getItem().getPrice() * item.getQuantity()) // 각 OrderItem의 가격을 quantity와 곱함
                .sum(); // 모든 가격을 더함
    }

    /**
     * 단건 주문 총 가격을 redis 에 30일간 캐싱한다.
     * @param posId     포스 식별자
     * @param salePrice 주문 총 가격
     */
    private void updateSalesInRedis(UUID posId, long salePrice) {
        // Redis 키 생성
        String key = "pos:" + posId + ":sales";
        // 현재 시간 (밀리초)
        String currentDate = String.valueOf(System.currentTimeMillis());
        // 매출 항목 저장: {timestamp: salesAmount}
        redisUtil.hset(key, currentDate, String.valueOf(salePrice));
        // 만료 설정: 30일 후에 자동으로 삭제
        redisUtil.expire(key, 30 * 24 * 60 * 60); // 30일
    }

    /**
     * 특정 포스의 30일간 총 판매액을 반환한다.
     * @param posId 포스 식별자
     * @return 30일간의 총 판매액
     */
    public long getTotalSales(UUID posId) {
        //key 생성
        String key = "pos:" + posId + ":sales";
        // 모든 매출 항목 가져오기
        Map<String, String> salesMap = redisUtil.hgetAll(key);

        long totalSales = 0;
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, String> entry : salesMap.entrySet()) {
            long timestamp = Long.parseLong(entry.getKey());
            // 30일이 지난 항목은 제외
            if (currentTime - timestamp <= 30 * 24 * 60 * 60 * 1000L) {
                totalSales += Long.parseLong(entry.getValue());
            }
        }
        return totalSales;
    }

    /**
     * 같은 code_id를 가진 Pos 들의 30일 총 판매 평균액, Pos 의 수, 현재 Pos 의 등수를 반환한다.
     * @param posId 현재 Pos 의 식별자
     * @return PosStatisticsDto
     */
    public PosStatisticsResponse getPosStatistics(UUID posId) {
        // 현재 Pos 찾기
        Pos currentPos = posRepository.findById(posId).orElseThrow(() -> new IllegalArgumentException("Pos not found"));
        UUID codeId = currentPos.getCode().getId();

        // 같은 code_id를 가진 모든 Pos 찾기
        List<Pos> posList = posRepository.findByCodeId(codeId);

        // 각 Pos 의 30일 총 판매액 계산
        List<Long> totalSalesList = posList.stream()
                .map(pos -> getTotalSales(pos.getId()))
                .toList();

        // 전체 판매액
        long totalSales = totalSalesList.stream().mapToLong(Long::longValue).sum();
        // Pos 의 수
        long posCount = posList.size();
        // 평균 판매액 계산
        long averageSales = posCount > 0 ? totalSales / posCount : 0;

        // 현재 Pos 의 총 판매액
        long currentPosSales = getTotalSales(posId);
        // 현재 Pos 의 등수 계산
        long currentPosRank = totalSalesList.stream()
                // 현재 Pos 보다 판매액이 높은 수를 카운트
                .filter(sales -> sales > currentPosSales)
        // 현재 Pos 의 등수 (1부터 시작)
                .count() + 1;

        return new PosStatisticsResponse(averageSales, posCount, currentPosRank);
    }

    public List<OrderDetailResponse> getOrderItemsByDuration(OrderDurationRequest request) {
        log.info("[getOrderItemsByDuration] 기간별 주문 조회 시작. 시작일: {}, 종료일: {}", request.getStartDate(), request.getEndDate());

        // 현재 Pos 찾기
        Pos currentPos = userService.getCurrentPos();
        LocalDateTime startDateTime = LocalDate.parse(request.getStartDate(), DateTimeFormatter.ISO_DATE).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(request.getEndDate(), DateTimeFormatter.ISO_DATE).atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findAllByPosIdAndCreatedDateBetweenOrderByCreatedDate(currentPos.getId(),startDateTime, endDateTime);

        if (orders.isEmpty()) {
            log.info("[getOrderItemsByDuration] 해당 기간 동안의 주문이 없습니다.");
            return Collections.emptyList();
        }

        List<OrderDetailResponse> orderDetails = orders.stream().map(order -> {
            OrderDetailResponse detail = new OrderDetailResponse(order);

            List<OrderItemDetail> itemDetails = order.getItems().stream()
                    .map(OrderItemDetail::new)
                    .collect(Collectors.toList());

            detail.updateItems(itemDetails);
            return detail;
        }).collect(Collectors.toList());

        log.info("[getOrderItemsByDuration] 기간별 주문 조회 완료. 조회된 주문 수: {}", orderDetails.size());

        return orderDetails;
    }

    /**
     * 현재 로그인된 포스의 가장 오래된 주문일자를 반환하는 메서드
     */
    public LocalDate getOldestOrderDate() {
        UUID currentPosId = userService.getCurrentPosId();
        return orderRepository.findOldestOrderDateByPosId(currentPosId)
                .map(LocalDateTime::toLocalDate)
                .orElseThrow(() -> new NoSuchElementException("No orders found for the current POS"));
    }
}
