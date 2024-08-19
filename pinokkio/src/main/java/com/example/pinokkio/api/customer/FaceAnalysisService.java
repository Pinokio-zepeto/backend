package com.example.pinokkio.api.customer;

import com.example.pinokkio.api.customer.dto.response.AnalysisResult;
import com.example.pinokkio.api.customer.sse.SSEService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class FaceAnalysisService {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    @Value("${redis.cache.ttl}")
    private long redisCacheTTL;

    private final RestTemplate restTemplate;
    private final SSEService sseService;
    private final CustomerService customerService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 이미지 리스트를 반환한 결
     * @param images 이미지 리스트
     * @return 이미지 리스트를 분석한 결과를
     */
    public AnalysisResult analyzeImages(UUID kioskId, List<String> images) throws JsonProcessingException {
        log.info("Starting image analysis for {} images.", images.size());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("images", images);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("Sending request to FastAPI at {}.", fastApiUrl + "/fast/analyze_faces");
            ResponseEntity<Map> response = restTemplate.exchange(
                    fastApiUrl + "/fast/analyze_faces",
                    HttpMethod.POST,
                    request,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Received successful response from FastAPI with status code {}.", response.getStatusCode());

                if (response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    log.debug("Response body: {}", responseBody);

                    if (responseBody.containsKey("result")) {
                        Map<String, Object> resultMap = (Map<String, Object>) responseBody.get("result");
                        String embeddingString = objectMapper.writeValueAsString(resultMap.get("encrypted_embedding"));
                        String encodedEmbedding = Base64.getEncoder().encodeToString(embeddingString.getBytes());

                        AnalysisResult result = new AnalysisResult(
                                ((Number) resultMap.get("age")).intValue(),
                                (String) resultMap.get("gender"),
                                (Boolean) resultMap.get("is_face"),
                                encodedEmbedding
                        );

                        log.info("Analysis result processed: Age - {}, Gender - {}, Is Face - {}, faceEmbedding - {}",
                                result.getAge(), result.getGender(), result.isFace(), result.getEncryptedEmbedding());

                        if (result.isFace()) {
                            log.debug("Face detected. Caching analysis result and proceeding with customer search.");
                            cacheAnalysisResult(result);
                            customerService.findCustomer(kioskId, result);
                            return result;
                        } else {
                            log.info("No face detected. Sending waiting event.");
                            sseService.sendWaitingEvent(false);
                            return null;
                        }
                    }
                }
            } else {
                log.warn("Received non-successful response from FastAPI: {}", response.getStatusCode());
            }

            log.info("No valid result found in response. Sending waiting event.");
            sseService.sendWaitingEvent(false);
            return null;
        } catch (RestClientException e) {
            log.error("Error occurred while communicating with FastAPI: {}", e.getMessage(), e);
            sseService.sendWaitingEvent(false);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred: {}", e.getMessage(), e);
            sseService.sendWaitingEvent(false);
            throw e;
        }
    }

    /**
     * 분석 결과를 Redis 에 캐싱하는 메소드
     * @param result 분석 결과
     */
    private void cacheAnalysisResult(AnalysisResult result) {
        String cacheKey = "analysis_result:" + result.getEncryptedEmbedding();
        try {
            log.debug("Caching analysis result with key: {}", cacheKey);
            String jsonResult = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, jsonResult, redisCacheTTL, TimeUnit.SECONDS);
            log.info("Analysis result cached successfully with TTL of {} seconds.", redisCacheTTL);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while serializing analysis result for caching: {}", e.getMessage(), e);
        }
    }

}
