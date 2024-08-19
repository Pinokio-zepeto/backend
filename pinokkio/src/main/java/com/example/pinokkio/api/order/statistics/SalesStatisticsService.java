package com.example.pinokkio.api.order.statistics;

import com.example.pinokkio.api.order.Order;
import com.example.pinokkio.api.order.OrderRepository;
import com.example.pinokkio.api.order.statistics.dto.response.GroupSalesStatisticsResponse;
import com.example.pinokkio.api.order.statistics.dto.response.SalesStatisticsResponse;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.config.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SalesStatisticsService {

    private final RedisUtil redisUtil;
    private final OrderRepository orderRepository;
    private final SalesStatisticsRepository salesStatisticsRepository;
    private final PosRepository posRepository;
    private final UserService userService;

    @Scheduled(cron = "0 0 1 * * *")  // 매일 새벽 1시에 실행
    public void aggregateDailySales() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        aggregateSales(yesterday, PeriodType.DAILY);
    }

    @Scheduled(cron = "0 0 1 * * MON")  // 매주 월요일 새벽 1시에 실행
    public void aggregateWeeklySales() {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        aggregateSales(lastWeek, PeriodType.WEEKLY);
    }

    @Scheduled(cron = "0 0 1 1 * *")  // 매월 1일 새벽 1시에 실행
    public void aggregateMonthlySales() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        aggregateSales(lastMonth, PeriodType.MONTHLY);
    }

    @Scheduled(cron = "0 0 1 1 1 *")  // 매년 1월 1일 새벽 1시에 실행
    public void aggregateYearlySales() {
        LocalDate lastYear = LocalDate.now().minusYears(1);
        aggregateSales(lastYear, PeriodType.YEARLY);
    }

    public void aggregateSales(LocalDate date, PeriodType periodType) {
        List<UUID> allPosIds = posRepository.findAllPosId();
        for (UUID posId : allPosIds) {
            aggregateSalesForPos(posId, date, periodType);
        }
    }

    private void aggregateSalesForPos(UUID posId, LocalDate date, PeriodType periodType) {
        int year = date.getYear();
        int period = getPeriod(date, periodType);

        LocalDateTime start = getStartDateTime(date, periodType);
        LocalDateTime end = getEndDateTime(date, periodType);

        List<Order> orders = orderRepository.findAllByPosIdAndCreatedDateBetweenOrderByCreatedDate(posId, start, end);
        log.info("Found {} orders for posId: {}, date: {}, periodType: {}", orders.size(), posId, date, periodType);

        long totalSales = orders.stream().mapToLong(Order::getTotalPrice).sum();
        log.info("Total sales for posId: {}, date: {}, periodType: {}: {}", posId, date, periodType, totalSales);

        SalesStatistics salesStatistics = salesStatisticsRepository
                .findByPosIdAndPeriodTypeAndYearAndPeriodOrderByYearAscPeriodAsc(posId, periodType, year, period)
                .orElseGet(() -> new SalesStatistics(posId, year, period, periodType, 0));

        salesStatistics.setTotalSales(totalSales);
        salesStatisticsRepository.save(salesStatistics);
        updateRedisCache(salesStatistics);
    }

    private static int getPeriod(LocalDate date, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date.getDayOfYear();
            case WEEKLY -> date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            case MONTHLY -> date.getMonthValue();
            case YEARLY -> 1;
        };
    }

    public GroupSalesStatisticsResponse getSalesStatistics(LocalDate startDate, LocalDate endDate) {
        UUID posId = userService.getCurrentPosId();

        List<SalesStatisticsResponse> dailySales = getSalesStatistics(posId, startDate, endDate, PeriodType.DAILY);
        List<SalesStatisticsResponse> weeklySales = getSalesStatistics(posId, startDate, endDate, PeriodType.WEEKLY);
        List<SalesStatisticsResponse> monthlySales = getSalesStatistics(posId, startDate, endDate, PeriodType.MONTHLY);
        List<SalesStatisticsResponse> yearlySales = getSalesStatistics(posId, startDate, endDate, PeriodType.YEARLY);

        return new GroupSalesStatisticsResponse(dailySales, weeklySales, monthlySales, yearlySales);
    }

    private List<SalesStatisticsResponse> getSalesStatistics(UUID posId, LocalDate startDate, LocalDate endDate, PeriodType periodType) {
        List<SalesStatisticsResponse> result = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            LocalDate periodEndDate = getEndDate(currentDate.getYear(), getPeriod(currentDate, periodType), periodType);
            if (periodEndDate.isAfter(endDate)) {
                periodEndDate = endDate;
            }

            int year = currentDate.getYear();
            int period = getPeriod(currentDate, periodType);

            Optional<SalesStatistics> salesStatistics = salesStatisticsRepository
                    .findByPosIdAndPeriodTypeAndYearAndPeriodOrderByYearAscPeriodAsc(posId, periodType, year, period);

            long totalSales = salesStatistics
                    .map(sales -> {
                        Optional<Long> cachedSales = getFromRedisCache(posId, periodType, year, period);
                        return cachedSales.orElse(sales.getTotalSales());
                    })
                    .orElse(0L);

            result.add(new SalesStatisticsResponse(currentDate, periodEndDate, totalSales));

            currentDate = periodEndDate.plusDays(1);
        }

        return result;
    }

    private LocalDate getEndDate(int year, int period, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> LocalDate.ofYearDay(year, period);
            case WEEKLY -> LocalDate.ofYearDay(year, 1).with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, period).plusDays(6);
            case MONTHLY -> LocalDate.of(year, period, 1).plusMonths(1).minusDays(1);
            case YEARLY -> LocalDate.of(year, 12, 31);
        };
    }

    @Transactional
    public void updateSalesStatisticsOnOrderChange(Order order, long priceDifference) {
        LocalDate orderDate = order.getCreatedDate().toLocalDate();
        updateSalesStatistics(order.getPos().getId(), orderDate, PeriodType.DAILY, priceDifference);
        updateSalesStatistics(order.getPos().getId(), orderDate, PeriodType.WEEKLY, priceDifference);
        updateSalesStatistics(order.getPos().getId(), orderDate, PeriodType.MONTHLY, priceDifference);
        updateSalesStatistics(order.getPos().getId(), orderDate, PeriodType.YEARLY, priceDifference);
    }

    private void updateSalesStatistics(UUID posId, LocalDate date, PeriodType periodType, long priceDifference) {
        int year = date.getYear();
        int period = getPeriod(date, periodType);

        SalesStatistics salesStatistics = salesStatisticsRepository
                .findByPosIdAndPeriodTypeAndYearAndPeriodOrderByYearAscPeriodAsc(posId, periodType, year, period)
                .orElseGet(() -> new SalesStatistics(posId, year, period, periodType, 0));

        salesStatistics.setTotalSales(salesStatistics.getTotalSales() + priceDifference);
        salesStatisticsRepository.save(salesStatistics);
        updateRedisCache(salesStatistics);
    }

    private String getCacheKey(UUID posId, PeriodType periodType, int year, int period) {
        return String.format("sales:%s:%s:%d:%d", posId, periodType, year, period);
    }

    private void updateRedisCache(SalesStatistics sales) {
        String cacheKey = getCacheKey(sales.getPosId(), sales.getPeriodType(), sales.getYear(), sales.getPeriod());
        redisUtil.setDataExpire(cacheKey, String.valueOf(sales.getTotalSales()), 24 * 60); // 24시간 캐시 (분 단위)
    }

    private Optional<Long> getFromRedisCache(UUID posId, PeriodType periodType, int year, int period) {
        String cacheKey = getCacheKey(posId, periodType, year, period);
        String cachedValue = redisUtil.getData(cacheKey);
        return Optional.ofNullable(cachedValue).map(Long::parseLong);
    }

    @Transactional
    public void backfillSalesStatistics(LocalDate startDate, LocalDate endDate) {
        List<UUID> allPosIds = posRepository.findAllPosId();
        for (UUID posId : allPosIds) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                aggregateSalesForPos(posId, date, PeriodType.DAILY);
                aggregateSalesForPos(posId, date, PeriodType.WEEKLY);
                aggregateSalesForPos(posId, date, PeriodType.MONTHLY);
                aggregateSalesForPos(posId, date, PeriodType.YEARLY);
            }
        }
    }

    private LocalDateTime getStartDateTime(LocalDate date, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date.atStartOfDay();
            case WEEKLY -> date.with(DayOfWeek.MONDAY).atStartOfDay();
            case MONTHLY -> date.withDayOfMonth(1).atStartOfDay();
            case YEARLY -> date.withDayOfYear(1).atStartOfDay();
        };
    }

    private LocalDateTime getEndDateTime(LocalDate date, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> date.atTime(LocalTime.MAX);
            case WEEKLY -> date.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
            case MONTHLY -> date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX);
            case YEARLY -> date.withDayOfYear(date.lengthOfYear()).atTime(LocalTime.MAX);
        };
    }
}