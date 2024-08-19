package com.example.pinokkio.api.kiosk;

import com.example.pinokkio.api.kiosk.dto.response.KioskResponse;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.config.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Kiosk Controller", description = "키오스크 정보 관련 API")
public class KioskController {

    private final KioskService kioskService;
    private final JwtProvider jwtProvider;
    private final UserService userService;

    /**
     * 요청한 키오스크의 자기 정보 조회
     * [연결된 포스 ID, 키오스크 본인 ID, 키오스크 이메일]
     */
    @Operation(summary = "키오스크 본인정보 조회", description = "키오스크 본인정보 조회")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    @GetMapping("/kiosk/my-info")
    public ResponseEntity<?> getKiosk() {
        KioskResponse kioskResponse = kioskService.getKioskInfo(userService.getCurrentKiosk());
        return ResponseEntity.ok(kioskResponse);
    }

    /**
     * 키오스크 회원탈퇴
     * @return ResponseEntity
     */
    @Operation(summary = "키오스크 회원탈퇴", description = "키오스크 회원탈퇴")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    @DeleteMapping("/kiosk/delete")
    public ResponseEntity<?> deleteKiosk() {
        Kiosk kiosk = userService.getCurrentKiosk();
        kioskService.deleteKiosk(kiosk.getId());
        return ResponseEntity.noContent().build();

    }
}
