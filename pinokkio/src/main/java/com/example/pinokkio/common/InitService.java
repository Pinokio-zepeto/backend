package com.example.pinokkio.common;

import com.example.pinokkio.api.category.Category;
import com.example.pinokkio.api.category.CategoryRepository;
import com.example.pinokkio.api.customer.Customer;
import com.example.pinokkio.api.customer.CustomerRepository;
import com.example.pinokkio.api.item.Item;
import com.example.pinokkio.api.item.ItemRepository;
import com.example.pinokkio.api.kiosk.Kiosk;
import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.order.Order;
import com.example.pinokkio.api.order.OrderRepository;
import com.example.pinokkio.api.order.orderitem.OrderItem;
import com.example.pinokkio.api.order.statistics.SalesStatistics;
import com.example.pinokkio.api.order.statistics.SalesStatisticsService;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.pos.code.Code;
import com.example.pinokkio.api.pos.code.CodeRepository;
import com.example.pinokkio.api.teller.Teller;
import com.example.pinokkio.api.teller.TellerRepository;
import com.example.pinokkio.common.type.Gender;
import com.example.pinokkio.common.type.OrderStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InitService implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${default-image}")
    private String NO_IMAGE_URL;

    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private final CodeRepository codeRepository;
    private final CategoryRepository categoryRepository;
    private final PosRepository posRepository;
    private final KioskRepository kioskRepository;
    private final CustomerRepository customerRepository;
    private final TellerRepository tellerRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    private final SalesStatisticsService salesStatisticsService;

    private final EntityManager entityManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        makeInitData();
        createDummyOrders();
    }

    private void makeInitData() {
        // Code Repository 데이터 추가
        Code starbucksCode = codeRepository.save(new Code("starbucks"));
        Code tomntomsCoffeeCode = codeRepository.save(new Code("tomntoms"));
        Code hollysCode = codeRepository.save(new Code("hollys"));

        // Pos Repository 데이터 추가 + 비고객 생성
        Pos starbucks = posRepository.save(Pos.builder()
                .code(starbucksCode)
                .email("pos1@starbucks.com")
                .password(passwordEncoder.encode("1234"))
                .build());
        Customer starbucksCustomer = customerRepository.save(Customer.builder()
                .phoneNumber("00000000")
                .pos(starbucks)
                .age(99)
                .gender(Gender.MALE)
                .faceEmbedding(null)
                .build());
        starbucks.updateDummyCustomerUUID(starbucksCustomer.getId());


        Pos tomntomsCoffee = posRepository.save(Pos.builder()
                .code(tomntomsCoffeeCode)
                .email("pos1@tomntoms.com")
                .password(passwordEncoder.encode("1234"))
                .build());
        Customer tomntomsCustomer = customerRepository.save(Customer.builder()
                .phoneNumber("00000000")
                .pos(tomntomsCoffee)
                .age(99)
                .gender(Gender.MALE)
                .faceEmbedding(null)
                .build());
        tomntomsCoffee.updateDummyCustomerUUID(tomntomsCustomer.getId());


        Pos hollys = posRepository.save(Pos.builder()
                .code(hollysCode)
                .email("pos1@hollys.com")
                .password(passwordEncoder.encode("1234"))
                .build());
        Customer hollysCustomer = customerRepository.save(Customer.builder()
                .phoneNumber("00000000")
                .pos(hollys)
                .age(99)
                .gender(Gender.MALE)
                .faceEmbedding(null)
                .build());
        hollys.updateDummyCustomerUUID(hollysCustomer.getId());


        // Category Repository 데이터 추가
        Category espressoStarbucks = categoryRepository.save(Category.builder()
                .pos(starbucks)
                .name("에스프레소")
                .build());

        Category coldBrewStarbucks = categoryRepository.save(Category.builder()
                .pos(starbucks)
                .name("콜드 브루")
                .build());

        Category frappuccinoStarbucks = categoryRepository.save(Category.builder()
                .pos(starbucks)
                .name("프라푸치노")
                .build());

        Category blendedStarbucks = categoryRepository.save(Category.builder()
                .pos(starbucks)
                .name("블렌디드")
                .build());

        Category teaStarbucks = categoryRepository.save(Category.builder()
                .pos(starbucks)
                .name("티")
                .build());

        Category bakeryStarbucks = categoryRepository.save(Category.builder()
                .pos(starbucks)
                .name("베이커리")
                .build());

        Category mdStarbucks = categoryRepository.save(Category.builder()
                .pos(starbucks)
                .name("MD")
                .build());

        // 탐앤탐스 카테고리
        Category coffeeTomntoms = categoryRepository.save(Category.builder()
                .pos(tomntomsCoffee)
                .name("커피")
                .build());

        Category decafTomntoms = categoryRepository.save(Category.builder()
                .pos(tomntomsCoffee)
                .name("디카페인")
                .build());

        Category smoothieTomntoms = categoryRepository.save(Category.builder()
                .pos(tomntomsCoffee)
                .name("스무디")
                .build());

        Category teaAdeTomntoms = categoryRepository.save(Category.builder()
                .pos(tomntomsCoffee)
                .name("티/에이드")
                .build());

        Category bakeryTomntoms = categoryRepository.save(Category.builder()
                .pos(tomntomsCoffee)
                .name("베이커리")
                .build());

        Category mdTomntoms = categoryRepository.save(Category.builder()
                .pos(tomntomsCoffee)
                .name("MD")
                .build());

        // 할리스 카테고리
        Category coffeeHollys = categoryRepository.save(Category.builder()
                .pos(hollys)
                .name("커피")
                .build());

        Category decafHollys = categoryRepository.save(Category.builder()
                .pos(hollys)
                .name("디카페인")
                .build());

        Category smoothieHollys = categoryRepository.save(Category.builder()
                .pos(hollys)
                .name("스무디")
                .build());

        Category teaAideHollys = categoryRepository.save(Category.builder()
                .pos(hollys)
                .name("티/에이드")
                .build());

        Category bakeryHollys = categoryRepository.save(Category.builder()
                .pos(hollys)
                .name("베이커리")
                .build());

        Category mdHollys = categoryRepository.save(Category.builder()
                .pos(hollys)
                .name("MD")
                .build());

        // Kiosk Repository 데이터 추가
        addKiosks(starbucks);
        addKiosks(tomntomsCoffee);
        addKiosks(hollys);

        // Teller Repository 데이터 추가
        addTellers(starbucks);
        addTellers(tomntomsCoffee);
        addTellers(hollys);

        // Item Repository 데이터 추가
        addStarbucksItems(starbucks, espressoStarbucks, coldBrewStarbucks, frappuccinoStarbucks, blendedStarbucks, teaStarbucks, bakeryStarbucks, mdStarbucks);
        addItems(tomntomsCoffee, coffeeTomntoms, decafTomntoms, smoothieTomntoms, teaAdeTomntoms, bakeryTomntoms, mdTomntoms);
        addItems(hollys, coffeeHollys, decafHollys, smoothieHollys, teaAideHollys, bakeryHollys, mdHollys);
    }

    private void addKiosks(Pos pos) {
        String domain = pos.getCode().getName();
        for (int i = 1; i <= 20; i++) {
            String email = "kiosk" + i + "@" + domain + ".com";
            kioskRepository.save(new Kiosk(pos, email, passwordEncoder.encode("1234")));
        }
    }

    private void addTellers(Pos pos) {
        String domain = pos.getCode().getName();
        for (int i = 1; i <= 20; i++) {
            String email = "teller" + i + "@" + domain + ".com";
            tellerRepository.save(new Teller(pos.getCode(), email, passwordEncoder.encode("1234")));
        }
    }

    private void addItems(Pos pos, Category... categories) {
        for (Category category : categories) {
            switch (category.getName()) {
                case "커피":
                    itemRepository.save(new Item(pos, category, 4500, 100, "아메리카노", "깊고 진한 에스프레소와 뜨거운 물을 섞은 커피", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5000, 100, "카페라떼", "에스프레소와 스팀 밀크를 섞은 부드러운 커피", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5500, 100, "카푸치노", "에스프레소에 스팀 밀크와 거품을 얹은 커피", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5500, 100, "바닐라 라떼", "바닐라 시럽이 들어간 카페라떼", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "카라멜 마키아토", "바닐라 시럽과 카라멜 드리즐이 들어간 카페라떼", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5000, 100, "에스프레소", "진한 에스프레소 샷", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "모카", "초콜릿 시럽이 들어간 카페라떼", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "콜드브루", "차갑게 우려낸 깔끔한 커피", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 7000, 100, "니트로 콜드브루", "질소가 주입된 부드러운 콜드브루", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "플랫화이트", "진한 에스프레소와 스팀 밀크를 섞은 커피", NO_IMAGE_URL));
                    break;
                case "디카페인":
                    itemRepository.save(new Item(pos, category, 5000, 100, "디카페인 아메리카노", "카페인을 제거한 에스프레소로 만든 아메리카노", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5500, 100, "디카페인 카페라떼", "카페인을 제거한 에스프레소와 스팀 밀크를 섞은 라떼", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "디카페인 카푸치노", "카페인을 제거한 에스프레소에 스팀 밀크와 거품을 얹은 커피", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "디카페인 바닐라 라떼", "카페인을 제거한 에스프레소로 만든 바닐라 라떼", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "디카페인 카라멜 마키아토", "카페인을 제거한 에스프레소로 만든 카라멜 마키아토", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5500, 100, "디카페인 에스프레소", "카페인을 제거한 진한 에스프레소 샷", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "디카페인 모카", "카페인을 제거한 에스프레소로 만든 모카", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 7000, 100, "디카페인 콜드브루", "카페인을 제거한 원두로 만든 콜드브루", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 7500, 100, "디카페인 니트로 콜드브루", "카페인을 제거한 원두로 만든 니트로 콜드브루", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "디카페인 플랫화이트", "카페인을 제거한 에스프레소로 만든 플랫화이트", NO_IMAGE_URL));
                    break;
                case "스무디":
                    itemRepository.save(new Item(pos, category, 6000, 100, "딸기 스무디", "신선한 딸기로 만든 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "망고 스무디", "달콤한 망고로 만든 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "블루베리 스무디", "새콤달콤한 블루베리로 만든 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "요구르트 스무디", "부드러운 요구르트로 만든 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "키위 스무디", "상큼한 키위로 만든 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "패션후르츠 스무디", "이국적인 패션후르츠로 만든 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 7000, 100, "그린 스무디", "신선한 채소와 과일로 만든 건강 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 7000, 100, "초코 바나나 스무디", "초콜릿과 바나나가 어우러진 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 7000, 100, "복숭아 스무디", "달콤한 복숭아로 만든 스무디", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 7500, 100, "베리 믹스 스무디", "다양한 베리가 섞인 스무디", NO_IMAGE_URL));
                    break;
                case "티/에이드":
                    itemRepository.save(new Item(pos, category, 5000, 100, "얼그레이 티", "향긋한 얼그레이 티", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5000, 100, "페퍼민트 티", "상쾌한 페퍼민트 티", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5000, 100, "캐모마일 티", "은은한 캐모마일 티", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5500, 100, "잉글리시 브렉퍼스트 티", "진한 맛의 잉글리시 브렉퍼스트 티", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5500, 100, "자스민 티", "향긋한 자스민 티", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "레몬에이드", "상큼한 레몬에이드", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "자몽에이드", "새콤달콤한 자몽에이드", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6000, 100, "청포도에이드", "달콤한 청포도에이드", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "패션후르츠에이드", "이국적인 패션후르츠에이드", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 6500, 100, "블루베리에이드", "새콤달콤한 블루베리에이드", NO_IMAGE_URL));
                    break;
                case "베이커리":
                    itemRepository.save(new Item(pos, category, 3500, 100, "크로아상", "바삭한 크로아상", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 4000, 100, "초코 머핀", "초콜릿이 가득한 머핀", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 4000, 100, "블루베리 머핀", "블루베리가 가득한 머핀", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 4500, 100, "치즈케이크", "부드러운 치즈케이크", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 4500, 100, "티라미수", "커피 향이 가득한 티라미수", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 3000, 100, "플레인 베이글", "쫄깃한 플레인 베이글", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 3500, 100, "크림치즈 베이글", "크림치즈를 곁들인 베이글", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 5000, 100, "레드벨벳 케이크", "달콤한 레드벨벳 케이크", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 3500, 100, "스콘", "버터 향이 가득한 스콘", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 4000, 100, "당근 케이크", "건강한 당근 케이크", NO_IMAGE_URL));
                    break;
                case "MD":
                    itemRepository.save(new Item(pos, category, 15000, 50, "텀블러", "스테인리스 스틸 텀블러", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 20000, 50, "머그컵", "도자기 머그컵", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 25000, 30, "보온병", "스테인리스 스틸 보온병", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 10000, 100, "에코백", "친환경 에코백", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 8000, 100, "키체인", "귀여운 키체인", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 30000, 20, "플레이트", "도자기 플레이트", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 12000, 50, "티 스푼", "스테인리스 스틸 티 스푼", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 35000, 30, "프렌치프레스", "유리 프렌치프레스", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 18000, 50, "물병", "유리 물병", NO_IMAGE_URL));
                    itemRepository.save(new Item(pos, category, 22000, 40, "커피 저장 용기", "밀폐형 커피 저장 용기", NO_IMAGE_URL));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + category.getName());
            }
        }
    }

    private static final String BASE_URL = "https://pinokkio.s3.ap-northeast-2.amazonaws.com/item/starbucks/";
    private static final String FILE_EXTENSION = ".jpg";
    
    private static String encodeFileName(String fileName) {
        try {
            return BASE_URL + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replace("+", "%20") + FILE_EXTENSION;
        } catch (Exception e) {
            throw new RuntimeException("Error encoding file name: " + fileName, e);
        }
    }
    
    private void addStarbucksItems(Pos pos, Category... categories) {
        for (Category category : categories) {
            switch (category.getName()) {
                case "에스프레소":
                    itemRepository.save(new Item(pos, category, 4500, 100, "에스프레소", "강렬하고 짧은 한 모금의 커피", encodeFileName("에스프레소")));
                    itemRepository.save(new Item(pos, category, 4500, 100, "에스프레소 마키아토", "에스프레소와 소량의 우유 거품", encodeFileName("에스프레소 마키아토")));
                    itemRepository.save(new Item(pos, category, 4500, 100, "아메리카노", "에스프레소와 뜨거운 물", encodeFileName("아메리카노")));
                    itemRepository.save(new Item(pos, category, 5000, 100, "카페 라떼", "에스프레소와 스팀 밀크", encodeFileName("카페 라떼")));
                    itemRepository.save(new Item(pos, category, 5000, 100, "카푸치노", "에스프레소, 스팀 밀크, 우유 거품", encodeFileName("카푸치노")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "카라멜 마키아토", "바닐라 시럽, 에스프레소, 스팀 밀크, 카라멜 드리즐", encodeFileName("카라멜 마키아토")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "바닐라 라떼", "바닐라 시럽, 에스프레소, 스팀 밀크", encodeFileName("바닐라 라떼")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "헤이즐넛 스타벅스 더블 샷", "신선하게 제조된 더블 샷 믹스에 헤이즐넛 시럽을 넣고 에스프레소 샷, 얼음이 어우러져 핸드 쉐이킹한 음료", encodeFileName("헤이즐넛 스타벅스 더블 샷")));
                    itemRepository.save(new Item(pos, category, 6000, 100, "카페 모카", "모카 시럽, 에스프레소, 스팀 밀크, 휘핑크림", encodeFileName("카페 모카")));
                    itemRepository.save(new Item(pos, category, 6000, 100, "화이트 모카", "화이트 모카 시럽, 에스프레소, 스팀 밀크, 휘핑크림", encodeFileName("화이트 모카")));
                    break;
                case "콜드 브루":
                    itemRepository.save(new Item(pos, category, 5800, 100, "콜드 브루", "차가운 물로 추출한 부드러운 커피", encodeFileName("콜드 브루")));
                    itemRepository.save(new Item(pos, category, 6300, 100, "바닐라 크림 콜드 브루", "콜드 브루에 바닐라 크림을 올린 음료", encodeFileName("바닐라 크림 콜드 브루")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "니트로 콜드 브루", "질소 주입으로 부드러운 콜드 브루", encodeFileName("니트로 콜드 브루")));
                    itemRepository.save(new Item(pos, category, 7000, 100, "돌체 콜드 브루", "달콤한 연유 크림을 올린 콜드 브루", encodeFileName("돌체 콜드 브루")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "콜드 브루 플로트", "콜드 브루에 바닐라 아이스크림을 띄운 음료", encodeFileName("콜드 브루 플로트")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "콜드 브루 오트 라떼", "콜드 브루에 오트 밀크를 넣은 라떼", encodeFileName("콜드 브루 오트 라떼")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "막걸리향 크림 콜드 브루", "산에서 만날 수 있는 특별한 경험을 모티브로해 달콤, 향긋한 비알코올 막걸리 향 크림과 고소한 쌀 토핑으로 즐길 수 있는 콜드 브루", encodeFileName("막걸리향 크림 콜드 브루")));
                    itemRepository.save(new Item(pos, category, 7000, 100, "콜드 브루 몰트", "몰트 시럽이 들어간 콜드 브루", encodeFileName("콜드 브루 몰트")));
                    itemRepository.save(new Item(pos, category, 7200, 100, "시그니처 더 블랙 콜드 브루", "콜드 브루 전용 원두를 차가운 물로 매장에서 직접 추출하여 부드럽고 진한 풍미의 콜드브루를 언제 어디서나 편하게 즐겨보세요 (전용 보틀 /500ml)", encodeFileName("시그니처 더 블랙 콜드 브루")));
                    itemRepository.save(new Item(pos, category, 7500, 100, "민트 콜드 브루", "상쾌한 민트향 시럽과 잘게 갈린 얼음이 어우러져 시원함이 강렬하게 느껴지는 리저브만의 콜드 브루 음료", encodeFileName("민트 콜드 브루")));
                    break;
                case "프라푸치노":
                    itemRepository.save(new Item(pos, category, 7000, 100, "스타벅스 유니콘 프라푸치노", "매직과 드림을 담은 환상적인 프라푸치노", encodeFileName("스타벅스 유니콘 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "자몽 망고 코코 프라푸치노", "상큼한 자몽과 망고, 코코넛의 조화로운 프라푸치노", encodeFileName("자몽 망고 코코 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 6300, 100, "에스프레소 프라푸치노", "에스프레소 샷이 들어간 프라푸치노", encodeFileName("에스프레소 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 6300, 100, "자바 칩 프라푸치노", "커피, 모카 소스, 초콜릿 칩이 들어간 프라푸치노", encodeFileName("자바 칩 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 6300, 100, "카라멜 프라푸치노", "커피와 카라멜 시럽의 프라푸치노", encodeFileName("카라멜 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "딸기 글레이즈드 크림 프라푸치노", "달콤한 딸기 글레이즈와 크림의 조화로운 프라푸치노", encodeFileName("딸기 글레이즈드 크림 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 7000, 100, "제주 까망 크림 프라푸치노", "제주 흑임자를 활용한 고소한 크림 프라푸치노", encodeFileName("제주 까망 크림 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "제주 말차 크림 프라푸치노", "제주 녹차를 활용한 부드러운 크림 프라푸치노", encodeFileName("제주 말차 크림 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 7000, 100, "제주 쑥떡 크림 프라푸치노", "제주 쑥과 떡을 활용한 독특한 크림 프라푸치노", encodeFileName("제주 쑥떡 크림 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "초콜릿 크림 칩 프라푸치노", "초콜릿 칩이 들어간 달콤한 크림 프라푸치노", encodeFileName("초콜릿 크림 칩 프라푸치노")));
                    itemRepository.save(new Item(pos, category, 7000, 100, "화이트 타이거 프라푸치노", "화이트 초콜릿과 타이거 스트라이프의 독특한 프라푸치노", encodeFileName("화이트 타이거 프라푸치노")));
                    break;
                case "블렌디드":
                    itemRepository.save(new Item(pos, category, 6800, 100, "한라봉 천혜향 블렌디드", "제주 한라봉과 천혜향의 상큼한 조화", encodeFileName("한라봉 천혜향 블렌디드")));
                    itemRepository.save(new Item(pos, category, 7000, 100, "스타벅스 판다 쿠키 블렌디드", "귀여운 판다 쿠키가 들어간 특별한 블렌디드", encodeFileName("스타벅스 판다 쿠키 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "망고 패션 티 블렌디드", "망고와 패션프루트 티의 이국적인 블렌드", encodeFileName("망고 패션 티 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "북한산 레몬 얼 그레이 블렌디드", "상쾌한 레몬과 얼 그레이 티의 블렌드", encodeFileName("북한산 레몬 얼 그레이 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "스타벅스 클래식 밀크티 블렌디드", "클래식한 맛의 밀크티 블렌디드", encodeFileName("스타벅스 클래식 밀크티 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "여수 바다 유자 블렌디드", "여수의 상큼한 유자로 만든 블렌디드", encodeFileName("여수 바다 유자 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6300, 100, "딸기 딜라이트 요거트 블렌디드", "딸기와 요거트의 상큼한 블렌디드", encodeFileName("딸기 딜라이트 요거트 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6300, 100, "망고 바나나 블렌디드", "망고와 바나나의 달콤한 조화", encodeFileName("망고 바나나 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "제주 팔삭 자몽 허니 블렌디드", "제주 자몽과 꿀의 달콤새콤한 블렌드", encodeFileName("제주 팔삭 자몽 허니 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "코튼 스카이 요거트 블렌디드", "구름같이 부드러운 요거트 블렌디드", encodeFileName("코튼 스카이 요거트 블렌디드")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "피치 망고 블렌디드", "복숭아와 망고의 달콤한 블렌드", encodeFileName("피치 망고 블렌디드")));
                    break;
                case "티":
                    itemRepository.save(new Item(pos, category, 5500, 100, "잉글리시 브렉퍼스트 티", "깊고 그윽한 맛의 홍차", encodeFileName("잉글리시 브렉퍼스트 티")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "얼 그레이 티", "시트러스향 베르가못 향이 특징인 홍차", encodeFileName("얼 그레이 티")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "유스베리 티", "달콤한 베리류 블렌딩 티", encodeFileName("유스베리 티")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "제주 유기농 녹차로 만든 티", "맑은 수색과 신선한 향의 녹차", encodeFileName("제주 유기농 녹차로 만든 티")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "차이 티 라떼", "홍차에 다양한 스파이스를 더한 티 라떼", encodeFileName("차이 티 라떼")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "캐모마일 블렌드 티", "캐모마일과 히비스커스의 블렌드 티", encodeFileName("캐모마일 블렌드 티")));
                    itemRepository.save(new Item(pos, category, 6000, 100, "민트 블렌드 티", "스피어민트, 페퍼민트가 블렌딩된 상쾌한 허브티", encodeFileName("민트 블렌드 티")));
                    itemRepository.save(new Item(pos, category, 6000, 100, "히비스커스 블렌드 티", "히비스커스, 사과, 파파야의 상큼한 허브티", encodeFileName("히비스커스 블렌드 티")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "말차 라떼", "진한 말차와 우유의 조화", encodeFileName("말차 라떼")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "자몽 허니 블랙 티", "새콤한 자몽과 달콤한 꿀이 깊고 그윽한 풍미의 스타벅스 티바나 블랙 티의 조화", encodeFileName("자몽 허니 블랙 티")));
                    break;
                case "베이커리":
                    itemRepository.save(new Item(pos, category, 4500, 100, "마스카포네 크림 소라빵", "부드러운 마스카포네 크림을 채운 소라빵", encodeFileName("마스카포네 크림 소라빵")));
                    itemRepository.save(new Item(pos, category, 4700, 100, "탕종 블루베리 베이글", "쫄깃한 식감의 탕종 베이글에 블루베리를 더한 베이글", encodeFileName("탕종 블루베리 베이글")));
                    itemRepository.save(new Item(pos, category, 4900, 100, "클래식 스콘", "고소한 버터의 풍미가 가득한 스콘", encodeFileName("클래식 스콘")));
                    itemRepository.save(new Item(pos, category, 5200, 100, "피넛 쑥 떡 스콘", "고소한 피넛과 쑥, 떡을 넣어 만든 스콘", encodeFileName("피넛 쑥 떡 스콘")));
                    itemRepository.save(new Item(pos, category, 4800, 100, "거문 오름 크루아상", "제주 거문 오름을 형상화한 초코 크루아상", encodeFileName("거문 오름 크루아상")));
                    itemRepository.save(new Item(pos, category, 5500, 100, "베어리스타 마스카포네 도넛", "귀여운 베어리스타 모양의 마스카포네 크림 도넛", encodeFileName("베어리스타 마스카포네 도넛")));
                    itemRepository.save(new Item(pos, category, 4900, 100, "연유 밀크모닝", "달콤한 연유 크림을 넣은 부드러운 롤 케이크", encodeFileName("연유 밀크모닝")));
                    itemRepository.save(new Item(pos, category, 6500, 100, "레인보우 크레이프 케이크", "다채로운 색감의 레이어드 크레이프 케이크", encodeFileName("레인보우 크레이프 케이크")));
                    itemRepository.save(new Item(pos, category, 6900, 100, "바스크 초코 치즈 케이크", "진한 초콜릿과 치즈의 조화로운 바스크 스타일 케이크", encodeFileName("바스크 초코 치즈 케이크")));
                    itemRepository.save(new Item(pos, category, 7200, 100, "7 레이어 가나슈 케이크", "7개 층의 초콜릿 가나슈로 만든 케이크", encodeFileName("7 레이어 가나슈 케이크")));
                    itemRepository.save(new Item(pos, category, 6200, 100, "부드러운 생크림 카스텔라", "촉촉하고 부드러운 생크림을 넣은 카스텔라", encodeFileName("부드러운 생크림 카스텔라")));
                    itemRepository.save(new Item(pos, category, 5900, 100, "당근 현무암 케이크", "제주 현무암을 형상화한 당근 케이크", encodeFileName("당근 현무암 케이크")));
                    itemRepository.save(new Item(pos, category, 7500, 100, "마스카포네 티라미수", "마스카포네 치즈와 에스프레소의 풍미가 어우러진 티라미수", encodeFileName("마스카포네 티라미수")));
                    itemRepository.save(new Item(pos, category, 6800, 100, "콥 & 화이트 샐러드 밀 박스", "신선한 채소와 단백질이 어우러진 샐러드", encodeFileName("콥 & 화이트 샐러드 밀 박스")));
                    itemRepository.save(new Item(pos, category, 5900, 100, "에그에그 샌드위치", "부드러운 에그 샐러드를 듬뿍 넣은 샌드위치", encodeFileName("에그에그 샌드위치")));
                    break;
                case "MD":
                    itemRepository.save(new Item(pos, category, 28000, 50, "SS 코리아 단청 트로이 텀블러 473ml", "한국 전통 단청 문양이 새겨진 스테인리스 텀블러", encodeFileName("SS 코리아 단청 트로이 텀블러 473ml")));
                    itemRepository.save(new Item(pos, category, 32000, 50, "SS 스탠리 그레이 켄처 텀블러 1183ml", "대용량 스테인리스 스틸 텀블러", encodeFileName("SS 스탠리 그레이 켄처 텀블러 1183ml")));
                    itemRepository.save(new Item(pos, category, 25000, 50, "SS 쿨 써머 파인니 콜드컵 473ml", "여름 시즌 한정 스테인리스 콜드컵", encodeFileName("SS 쿨 써머 파인니 콜드컵 473ml")));
                    itemRepository.save(new Item(pos, category, 23000, 50, "사이렌 글라스 콜드컵 503ml", "사이렌 로고가 새겨진 유리 콜드컵", encodeFileName("사이렌 글라스 콜드컵 503ml")));
                    itemRepository.save(new Item(pos, category, 19000, 100, "코리아 단청 머그 355ml", "한국 전통 단청 문양의 세라믹 머그", encodeFileName("코리아 단청 머그 355ml")));
                    itemRepository.save(new Item(pos, category, 38000, 30, "JNM 하우스 보온병 480ml", "스타벅스 하우스 디자인의 보온병", encodeFileName("JNM 하우스 보온병 480ml")));
                    itemRepository.save(new Item(pos, category, 15000, 100, "스타벅스 하우스 에코백", "재사용 가능한 친환경 에코백", encodeFileName("스타벅스 하우스 에코백")));
                    itemRepository.save(new Item(pos, category, 12000, 100, "쿨 써머 베어리스타 키체인", "여름 시즌 한정 베어리스타 키체인", encodeFileName("쿨 써머 베어리스타 키체인")));
                    itemRepository.save(new Item(pos, category, 45000, 20, "SS 데스티 캠핑컬렉션 식기 세트 (6P)", "캠핑용 스테인리스 식기 세트", encodeFileName("SS 데스티 캠핑컬렉션 식기 세트 (6P)")));
                    itemRepository.save(new Item(pos, category, 35000, 30, "스타벅스 폴딩 랩 트레이", "접이식 노트북 테이블", encodeFileName("스타벅스 폴딩 랩 트레이")));
                    itemRepository.save(new Item(pos, category, 29000, 50, "사이렌 레버 드리퍼", "정밀한 추출이 가능한 커피 드리퍼", encodeFileName("사이렌 레버 드리퍼")));
                    itemRepository.save(new Item(pos, category, 22000, 50, "우드 핸들 글라스 서버 680ml", "나무 손잡이가 있는 유리 커피 서버", encodeFileName("우드 핸들 글라스 서버 680ml")));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + category.getName());
            }
        }
    }

    @Transactional
    public void createDummyOrders() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.now();

        List<Pos> allPos = entityManager.createQuery("SELECT p FROM Pos p", Pos.class).getResultList();

        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            int ordersPerDay = 10 + random.nextInt(11); // 10 to 20 orders per day
            for (int i = 0; i < ordersPerDay; i++) {
                Pos pos = allPos.get(random.nextInt(allPos.size()));

                // Get the customer associated with the selected POS
                Customer customer = entityManager.createQuery("SELECT c FROM Customer c WHERE c.pos.id = :posId", Customer.class)
                        .setParameter("posId", pos.getId())
                        .getSingleResult();

                List<Item> posItems = entityManager.createQuery("SELECT i FROM Item i WHERE i.pos.id = :posId", Item.class)
                        .setParameter("posId", pos.getId())
                        .getResultList();

                Order order = Order.builder()
                        .pos(pos)
                        .customer(customer)
                        .build();
                order.initializeItems();

                List<OrderItem> orderItems = createRandomOrderItems(order, customer.getId(), posItems);
                long totalPrice = orderItems.stream()
                        .mapToLong(item -> (long) item.getItem().getPrice() * item.getQuantity())
                        .sum();

                order.updateTotalPrice(totalPrice);

                // 주문 상태 결정 (80% ACTIVE, 20% CANCELLED)
                boolean isCancelled = random.nextDouble() < 0.2;
                if (isCancelled) {
                    order.toggleOrderStatus();
                }

                // 생성 날짜와 수정 날짜 설정
                LocalDateTime orderDateTime = generateRandomDateTime(date);
                LocalDateTime modifiedDateTime;

                if (order.getStatus() == OrderStatus.ACTIVE) {
                    modifiedDateTime = orderDateTime; // 활성 상태일 경우 생성일자와 수정일자 동일
                } else {
                    modifiedDateTime = generateRandomModifiedDateTime(orderDateTime, endDate); // 취소 상태일 경우 수정일자를 다르게 설정
                }

                // JPA Auditing을 우회하고 직접 날짜 설정
                entityManager.persist(order);
                entityManager.flush();
                entityManager.createQuery("UPDATE Order o SET o.createdDate = :createdDate, o.modifiedDate = :modifiedDate WHERE o.id = :id")
                        .setParameter("createdDate", orderDateTime)
                        .setParameter("modifiedDate", modifiedDateTime)
                        .setParameter("id", order.getId())
                        .executeUpdate();
            }
            entityManager.clear();
        }

        salesStatisticsService.backfillSalesStatistics(startDate, endDate);
    }
    private LocalDateTime generateRandomDateTime(LocalDate date) {
        return date.atTime(random.nextInt(24), random.nextInt(60), random.nextInt(60));
    }

    private LocalDateTime generateRandomModifiedDateTime(LocalDateTime createdDateTime, LocalDate endDate) {
        long minMinutes = 1;
        long maxMinutes = ChronoUnit.MINUTES.between(createdDateTime, endDate.atTime(23, 59, 59));
        long randomMinutes = minMinutes + (long) (random.nextDouble() * (maxMinutes - minMinutes));
        return createdDateTime.plusMinutes(randomMinutes);
    }

    private List<OrderItem> createRandomOrderItems(Order order, UUID customerId, List<Item> posItems) {
        int itemCount = 1 + random.nextInt(5); // 1 to 5 items per order
        List<OrderItem> orderItems = new ArrayList<>();

        for (int i = 0; i < itemCount; i++) {
            Item item = posItems.get(random.nextInt(posItems.size()));
            int quantity = 1 + random.nextInt(3); // 1 to 3 quantity for each item
            OrderItem orderItem = new OrderItem(order, item, customerId, quantity);
            orderItems.add(orderItem);
            order.getItems().add(orderItem);
        }

        return orderItems;
    }
}