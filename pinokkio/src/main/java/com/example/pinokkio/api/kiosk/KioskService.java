package com.example.pinokkio.api.kiosk;

import com.example.pinokkio.api.kiosk.dto.response.KioskResponse;
import com.example.pinokkio.exception.domain.kiosk.KioskNotFoundException;
import com.example.pinokkio.exception.domain.pos.PosEmailNotFoundException;
import com.example.pinokkio.grpc.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KioskService {

    private final KioskRepository kioskRepository;

    /**
     * 입력받은 이메일에 해당하는 키오스크의 핵심 필드를 반환한다.
     *
     * @param kiosk 키오스크 정보
     * @return KioskResponse
     */
    public KioskResponse getKioskInfo(Kiosk kiosk) {
        return new KioskResponse(
                kiosk.getId().toString(),
                kiosk.getPos().getId().toString(),
                kiosk.getEmail(),
                kiosk.getPos().getDummyCustomerUUID().toString()
        );
    }

    /**
     * gRPC 로그인 요청을 처리합니다.
     *
     * @param kioskId 키오스크 ID
     * @return LoginResponse 객체
     */
    public LoginResponse handleGrpcLogin(UUID kioskId) {
        log.info("Handling gRPC login for kiosk ID: {}", kioskId);
        try {
            log.info("Looking up kiosk with ID: {}", kioskId);
            Kiosk kiosk = kioskRepository.findById(kioskId).orElse(null);
            if (kiosk != null) {
                log.info("Kiosk found: {}", kiosk);
                return LoginResponse.newBuilder().setMessage("Login successful").build();
            } else {
                log.warn("Kiosk not found for ID: {}", kioskId);
                return LoginResponse.newBuilder().setMessage("Login failed").build();
            }
        } catch (NumberFormatException e) {
            log.error("Invalid kiosk ID format: {}", kioskId, e);
            return LoginResponse.newBuilder().setMessage("Invalid kiosk ID").build();
        }
    }

    /**
     * 키오스크 회원탈퇴
     *
     * @param kioskId 키오스크 ID
     */
    public void deleteKiosk(UUID kioskId) {
        log.info("회원탈퇴 요청: 키오스크 ID = {}", kioskId);
        Kiosk kiosk = kioskRepository
                .findById(kioskId)
                .orElseThrow(() -> new KioskNotFoundException(kioskId));
        kioskRepository.delete(kiosk);
        log.info("회원탈퇴 완료: 키오스크 ID = {}", kioskId);
    }

}