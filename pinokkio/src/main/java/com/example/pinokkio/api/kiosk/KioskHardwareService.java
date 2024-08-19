package com.example.pinokkio.api.kiosk;

import com.example.pinokkio.api.customer.FaceAnalysisService;
import com.example.pinokkio.api.customer.sse.SSEService;
import com.example.pinokkio.grpc.*;
import com.google.protobuf.ByteString;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class KioskHardwareService extends KioskServiceGrpc.KioskServiceImplBase {

    private static final String KIOSK_CONTROLLER_ADDRESS = "172.30.1.54";
    private static final int KIOSK_CONTROLLER_PORT = 3334;
    private static final int GRPC_SERVER_PORT = 3333;

    private Server grpcServer;

    private final SSEService sseService;
    private final FaceAnalysisService faceAnalysisService;
    private final KioskService kioskService;

    // gRPC 서버를 시작하는 메서드입니다.
    @PostConstruct
    public void startGrpcServer() {
        try {
            grpcServer = ServerBuilder.forPort(GRPC_SERVER_PORT)
                    .addService(this)
                    .build()
                    .start();
            log.info("gRPC server started on port " + GRPC_SERVER_PORT);
        } catch (IOException e) {
            log.error("Failed to start gRPC server", e);
        }
    }

    // gRPC 서버를 중지하는 메서드입니다.
    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null) {
            grpcServer.shutdown();
            try {
                if (!grpcServer.awaitTermination(5, TimeUnit.SECONDS)) {
                    grpcServer.shutdownNow();
                }
            } catch (InterruptedException e) {
                grpcServer.shutdownNow();
            }
        }
    }

    @Override
    public void captureImages(CaptureImagesRequest request, StreamObserver<CaptureImagesResponse> responseObserver) {
        UUID kioskId = UUID.fromString(request.getKioskId());
        log.info("Received captured images from kiosk {}. Initiating image analysis.", kioskId);

        sseService.sendWaitingEvent(true);

        List<String> base64Images = request.getImagesList().stream()
                .map(ByteString::toStringUtf8)
                .collect(Collectors.toList());

        try {
            faceAnalysisService.analyzeImages(kioskId, base64Images);
            log.info("Image analysis completed successfully for kiosk: {}", kioskId);

            CaptureImagesResponse response = CaptureImagesResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Images analyzed successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error during image analysis for kiosk: {}", kioskId, e);
            CaptureImagesResponse response = CaptureImagesResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error during image analysis: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } finally {
            sseService.sendWaitingEvent(false);
        }
    }

    // 키오스크의 밝기를 조절하는 메서드입니다.
    public CompletableFuture<Void> adjustBrightness(String kioskId, int brightness) {
        return CompletableFuture.runAsync(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(KIOSK_CONTROLLER_ADDRESS, KIOSK_CONTROLLER_PORT)
                    .usePlaintext()
                    .build();
            KioskServiceGrpc.KioskServiceFutureStub stub = KioskServiceGrpc.newFutureStub(channel);

            BrightnessRequest request = BrightnessRequest.newBuilder()
                    .setBrightness(brightness)
                    .build();

            try {
                stub.setBrightness(request).get();
            } catch (Exception e) {
                log.error("Error adjusting brightness", e);
            } finally {
                channel.shutdown();
            }
        });
    }

    // 키오스크를 리셋하는 메서드입니다.
    public CompletableFuture<Void> resetKiosk(String kioskId) {
        return CompletableFuture.runAsync(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(KIOSK_CONTROLLER_ADDRESS, KIOSK_CONTROLLER_PORT)
                    .usePlaintext()
                    .build();
            KioskServiceGrpc.KioskServiceBlockingStub stub = KioskServiceGrpc.newBlockingStub(channel);

            ResetRequest request = ResetRequest.newBuilder().build();

            try {
                stub.resetKiosk(request);
                log.info("Kiosk reset successful: {}", kioskId);
                sseService.sendWaitingEvent(false);
            } catch (StatusRuntimeException e) {
                log.error("Error resetting kiosk: {}", kioskId, e);
            } finally {
                channel.shutdown();
            }
        });
    }

    // 키오스크에 제어 신호를 보내는 메서드입니다.
    public void sendControlSignal(String kioskId, String signal) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(KIOSK_CONTROLLER_ADDRESS, KIOSK_CONTROLLER_PORT)
                .usePlaintext()
                .build();
        KioskServiceGrpc.KioskServiceBlockingStub stub = KioskServiceGrpc.newBlockingStub(channel);

        ControlSignalRequest request = ControlSignalRequest.newBuilder()
                .setSignal(signal)
                .build();

        try {
            stub.sendControlSignal(request);
            log.info("Control signal sent successfully: {} to kiosk: {}", signal, kioskId);
            sseService.sendWaitingEvent(true);
        } catch (StatusRuntimeException e) {
            log.error("Error sending control signal: {} to kiosk: {}", signal, kioskId, e);
            sseService.sendWaitingEvent(false);
        } finally {
            channel.shutdown();
        }
    }

    // gRPC 로그인 처리 메서드입니다.
    @Override
    public void kioskLogin(KioskId request, StreamObserver<LoginResponse> responseObserver) {
        LoginResponse response = kioskService.handleGrpcLogin(UUID.fromString(request.getId()));
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}