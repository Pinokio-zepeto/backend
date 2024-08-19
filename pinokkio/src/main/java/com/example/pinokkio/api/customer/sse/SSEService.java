package com.example.pinokkio.api.customer.sse;

import com.example.pinokkio.api.customer.Customer;
import com.example.pinokkio.api.customer.dto.response.AnalysisResult;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SSEService {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // 여러 클라이언트의 SseEmitter를 관리하기 위한 스레드 안전한 리스트입니다.
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SSEService() {
        scheduler.scheduleAtFixedRate(this::sendKeepAlive, 0, 15, TimeUnit.SECONDS);
        log.info("SSEService initialized and keep-alive scheduler started.");
    }

    private void sendKeepAlive() {
        log.debug("Sending keep-alive message to all connected clients.");
        Map<String, Object> keepAliveData = new HashMap<>();
        keepAliveData.put("status", "keep-alive");
        sendEventToAll("keepAlive", keepAliveData);
    }

    @PreDestroy
    public void destroy() {
        log.info("SSEService is being destroyed. Shutting down scheduler and completing emitters.");
        scheduler.shutdown();
        emitters.forEach(SseEmitter::complete);
    }

    // 새로운 SSE 연결을 생성하는 메서드입니다.
    public SseEmitter createEmitter() {
        log.info("Creating new SSE connection.");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.emitters.add(emitter);
        log.debug("Emitter added. Total emitters: {}", emitters.size());

        emitter.onCompletion(() -> {
            this.emitters.remove(emitter);
            log.info("SSE connection completed. Remaining emitters: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            this.emitters.remove(emitter);
            log.warn("SSE connection timed out. Remaining emitters: {}", emitters.size());
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected successfully"));
            log.info("Connected successfully message sent to client.");
        } catch (IOException e) {
            log.error("Error sending connect event: {}", e.getMessage());
            emitter.completeWithError(e);
        }
        return emitter;
    }

    // 대기 상태 변경 이벤트를 전송하는 메서드입니다.
    public void sendWaitingEvent(boolean isWaiting) {
        log.info("Sending waiting status event: {}", isWaiting);
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("waiting", isWaiting);
        sendEventToAll("waitingStatus", eventData);
    }

    // 얼굴 감지 결과를 전송하는 메서드입니다.
    public void sendFaceDetectionResult(boolean isFaceDetected) {
        log.info("Sending face detection result: {}", isFaceDetected);
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("isFace", isFaceDetected);
        sendEventToAll("faceDetectionResult", eventData);
    }

    // 얼굴 분석 결과와 고객 정보를 전송하는 메서드입니다.
    public void sendAnalysisResult(AnalysisResult analysisResult, Customer customer) {
        log.info("Sending analysis result. Age: {}, Gender: {}", analysisResult.getAge(), analysisResult.getGender());
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("age", analysisResult.getAge());
        eventData.put("gender", analysisResult.getGender());
        eventData.put("isFace", analysisResult.isFace());
        eventData.put("isCustomer", customer != null);
        eventData.put("faceEmbeddingData", analysisResult.getEncryptedEmbedding());

        if (customer != null) {
            log.debug("Customer information: ID: {}, Age: {}, Gender: {}", customer.getId(), customer.getAge(), customer.getGender());
            eventData.put("customerId", customer.getId());
            eventData.put("customerAge", customer.getAge());
            eventData.put("customerGender", customer.getGender());
        }

        sendEventToAll("analysisResult", eventData);
    }

    // 모든 연결된 클라이언트에게 이벤트를 전송하는 private 메서드입니다.
    private void sendEventToAll(String eventName, Map<String, Object> eventData) {
        log.debug("Sending event '{}' to all clients.", eventName);
        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : this.emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(eventData));
            } catch (IOException e) {
                log.error("IOException while sending event '{}': {}", eventName, e.getMessage());
                deadEmitters.add(emitter);
            } catch (Exception e) {
                log.error("Unexpected error while sending event '{}': {}", eventName, e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            this.emitters.removeAll(deadEmitters);
            log.warn("Removed {} dead emitters. Remaining emitters: {}", deadEmitters.size(), emitters.size());
        }
    }
}
