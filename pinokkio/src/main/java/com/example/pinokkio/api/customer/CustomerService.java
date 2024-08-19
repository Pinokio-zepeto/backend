package com.example.pinokkio.api.customer;

import com.example.pinokkio.api.customer.dto.response.AnalysisResult;
import com.example.pinokkio.api.customer.dto.response.CustomerResponse;
import com.example.pinokkio.api.customer.sse.SSEService;
import com.example.pinokkio.api.kiosk.Kiosk;
import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.kiosk.KioskService;
import com.example.pinokkio.api.kiosk.dto.response.KioskResponse;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.common.type.Gender;
import com.example.pinokkio.config.jwt.JwtProvider;
import com.example.pinokkio.exception.domain.customer.CustomerNotFoundException;
import com.example.pinokkio.exception.domain.pos.PosNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SSEService sseService;
    private final PosRepository posRepository;
    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;
    private final KioskService kioskService;
    private final UserService userService;
    private final KioskRepository kioskRepository;

    @Value("${redis.cache.ttl}")
    private long redisCacheTTL;

    // 고객과 유사도를 저장하는 내부 클래스
    private static class CustomerSimilarity {
        Customer customer;
        double similarity;

        CustomerSimilarity(Customer customer, double similarity) {
            this.customer = customer;
            this.similarity = similarity;
        }
    }

    /**
     * 얼굴 임베딩 정보와 함께 고객을 저장한다.
     */
    public CustomerResponse saveCustomer(AnalysisResult analysisResult, String phoneNumber) {
        byte[] faceEmbeddingData = Base64.getDecoder().decode(analysisResult.getEncryptedEmbedding());

        String cacheKey = "analysis_result:" + analysisResult.getEncryptedEmbedding();

        AnalysisResult cachedResult;
        String cachedString = redisTemplate.opsForValue().get(cacheKey);

        if (cachedString == null) {
            cachedResult = analysisResult;
            try {
                String jsonString = objectMapper.writeValueAsString(cachedResult);
                redisTemplate.opsForValue().set(cacheKey, jsonString, 30, TimeUnit.MINUTES);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                cachedResult = objectMapper.readValue(cachedString, AnalysisResult.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        byte[] embeddingData = Base64.getDecoder().decode(cachedResult.getEncryptedEmbedding());

        Pos currenetPos = getCurrenetPos();
        Customer customer = Customer.builder()
                .pos(currenetPos)
                .gender(Gender.fromString(cachedResult.getGender()))
                .phoneNumber(phoneNumber)
                .age(cachedResult.getAge())
                .faceEmbedding(embeddingData)
                .build();

        log.info("customer 등록: " + customer);
        Customer savedCustomer = customerRepository.save(customer);
        cacheCustomerEmbedding(savedCustomer.getId(), faceEmbeddingData);

        sseService.sendAnalysisResult(cachedResult, savedCustomer);

        return new CustomerResponse(savedCustomer);
    }

    // 헤더에 토큰이 있어야 확인 가능
    private Pos getCurrenetPos() {
        Kiosk kiosk = userService.getCurrentKiosk();
        KioskResponse kioskInfo = kioskService.getKioskInfo(kiosk);
        UUID posId = UUID.fromString(kioskInfo.getPosId());
        return posRepository.findById(posId)
                .orElseThrow(() -> new PosNotFoundException(posId));
    }

    /**
     * @param customerId 고객 식별자
     * @return 식별자로 탐색한 고객 정보
     */
    public Customer findById(UUID customerId) {
        return customerRepository
                .findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    /**
     * 얼굴 임베딩으로 고객을 찾는 메소드(**주요 로직**)
     *
     * @param age                    나이
     * @param gender                 성별
     * @param encryptedFaceEmbedding 암호화 상태의 얼굴 임베딩 정보
     * @return 매칭된 고객 정보
     */
    public Customer findCustomerByFaceEmbedding(UUID kioskId, int age, String gender, String encryptedFaceEmbedding) {
        try {
            // Redis 캐시 키를 생성합니다.
            String cacheKey = "face_embedding:" + encryptedFaceEmbedding;
            redisTemplate.opsForValue().set(cacheKey, encryptedFaceEmbedding, redisCacheTTL, TimeUnit.SECONDS);

            RealVector inputVector = parseEmbedding(encryptedFaceEmbedding);
            UUID posId = kioskRepository.findPosIdById(kioskId)
                    .orElseThrow(() -> new PosNotFoundException(kioskId));
            // 성별과 나이 범위 -> 범위 한정 탐색
            List<Customer> potentialCustomers = customerRepository.findByPosIdAndGenderAndAgeBetween(posId, Gender.valueOf(gender.toUpperCase()), age - 5, age + 5);
            Customer matchedCustomer = findMatchingCustomer(potentialCustomers, inputVector);

            // 매칭되는 고객이 없으면 모든 고객을 대상으로 다시 검색합니다.
            if (matchedCustomer == null) {
                List<Customer> allCustomers = customerRepository.findAllByPosId(posId);
                matchedCustomer = findMatchingCustomer(allCustomers, inputVector);
            }

            sseService.sendAnalysisResult(
                    new AnalysisResult(age, gender, true, encryptedFaceEmbedding),
                    matchedCustomer
            );
            return matchedCustomer;

        } catch (Exception e) {
            log.error("Error finding customer by face embedding", e);
            throw new RuntimeException("Error finding customer by face embedding", e);
        }
    }

    /**
     * 고객 목록에서 가장 유사한 고객을 찾는 로직
     *
     * @param customers   고객 목록
     * @param inputVector 벡터 정보
     * @return 유사도가 가장 높은 고객 정보
     */
    private Customer findMatchingCustomer(List<Customer> customers, RealVector inputVector) {
        //병렬 스트림 활용
        return customers.parallelStream()
                .map(customer -> processCustomer(customer, inputVector))
                // null이 아니고 유사도가 0.7 이상인 결과만 필터링합니다.
                .filter(cs -> cs != null && cs.similarity >= 0.7)
                .max(Comparator.comparingDouble(cs -> cs.similarity))
                .map(cs -> cs.customer)
                .orElse(null);
    }

    /**
     * 개별 고객의 얼굴 임베딩과 입력 벡터의 코사인 유사도를 계산하는 메서드
     *
     * @param customer    고객 정보
     * @param inputVector 벡터 정보
     * @return 얼굴 임베딩과 입력 벡터의 유사도
     */
    private CustomerSimilarity processCustomer(Customer customer, RealVector inputVector) {
        try {
            // 고객의 얼굴 임베딩 데이터를 조회합니다.
            byte[] faceEmbedding = Optional
                    .ofNullable(findById(customer.getId()))
                    .map(Customer::getFaceEmbedding)
                    .orElse(null);


            // Redis 캐시에서 고객의 얼굴 임베딩 데이터를 조회합니다.
            String cachedEmbedding = redisTemplate
                    .opsForValue()
                    .get("customer_embedding:" + customer.getId());

            if (cachedEmbedding == null) {
                cachedEmbedding = Base64.getEncoder().encodeToString(faceEmbedding);
                cacheCustomerEmbedding(customer.getId(), faceEmbedding);
            }

            RealVector customerVector = parseEmbedding(cachedEmbedding);

            // 고객의 얼굴 임베딩 데이터를 벡터로 변환합니다.
            return new CustomerSimilarity(
                    customer,
                    calculateCosineSimilarity(inputVector, customerVector)
            );

        } catch (Exception e) {
            log.error("Error processing customer {}", customer.getId(), e);
            return null;
        }
    }


    /**
     * 얼굴 임베딩 정보를 캐싱
     *
     * @param customerId    고객 식별자
     * @param faceEmbedding 얼굴 임베딩 정보
     */
    private void cacheCustomerEmbedding(UUID customerId, byte[] faceEmbedding) {
        try {
            String embeddingString = new String(faceEmbedding);
            String encodedEmbedding = Base64.getEncoder().encodeToString(embeddingString.getBytes());
            redisTemplate
                    .opsForValue()
                    .set(
                            "customer_embedding:" + customerId,
                            encodedEmbedding,
                            redisCacheTTL,
                            TimeUnit.SECONDS
                    );
        } catch (Exception e) {
            log.error("고객의 얼굴 임베딩 정보를 저장하는 과정에서 오류 발생 = {}", customerId, e);
        }
    }

    /**
     * 문자열 형태의 임베딩을 벡터로 변환하여 반환한다.
     *
     * @param embedding 문자열 형태의 임베딩
     * @return 임베딩을 벡터로 변환한 값
     */
    @Cacheable(value = "embeddingVectors", key = "#embedding")
    public RealVector parseEmbedding(String embedding) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(embedding);
            String decodedString = new String(decodedBytes);

            decodedString = decodedString.replaceAll("^\"|\"$", "");

            List<Double> values = objectMapper.readValue(decodedString, new TypeReference<List<Double>>() {
            });
            double[] doubleValues = values.stream().mapToDouble(Double::doubleValue).toArray();
            return new ArrayRealVector(doubleValues);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("임베딩 데이터 파싱 실패", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("임베딩 데이터 디코딩 실패", e);
        }
    }

    /**
     * 유사도 정보를 계산하여 반환한다.
     *
     * @param v1 벡터 정보 1
     * @param v2 벡터 정보 2
     * @return 벡터 유사도
     */
    private double calculateCosineSimilarity(RealVector v1, RealVector v2) {
        RealVector normalizedV1 = normalizeVector(v1);
        RealVector normalizedV2 = normalizeVector(v2);
        return normalizedV1.dotProduct(normalizedV2);
    }

    private RealVector normalizeVector(RealVector vector) {
        double norm = vector.getNorm();
        return norm > 0 ? vector.mapDivide(norm) : vector;
    }

    // 얼굴 분석 결과를 바탕으로 고객을 찾거나 등록하는 메서드
    public void findCustomer(UUID kioskId, AnalysisResult analysisResult) {
        // 얼굴 임베딩을 사용하여 고객을 찾습니다.
        Customer matchedCustomer = findCustomerByFaceEmbedding(
                kioskId,
                analysisResult.getAge(),
                analysisResult.getGender(),
                analysisResult.getEncryptedEmbedding());

        // 분석 결과와 매칭된 고객 정보를 SSE를 통해 전송합니다.
        sseService.sendAnalysisResult(analysisResult, matchedCustomer);
    }

    /**
     * 전화번호로 현재 POS 내 고객을 조회하는 메서드
     *
     * @param phoneNumber 전화번호 8자리
     * @return 고객 정보
     */
    public CustomerResponse findCustomerByPhoneNumber(String phoneNumber) {
        Kiosk currentKiosk = userService.getCurrentKiosk();
        Pos pos = currentKiosk.getPos();

        return customerRepository
                .findByPosIdAndPhoneNumber(pos.getId(), phoneNumber)
                .map(CustomerResponse::new)
                .orElseThrow(() -> new CustomerNotFoundException(phoneNumber));
    }
}