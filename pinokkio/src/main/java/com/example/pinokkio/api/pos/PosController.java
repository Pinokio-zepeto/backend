package com.example.pinokkio.api.pos;

import com.example.pinokkio.api.auth.dto.response.EmailDuplicationResponse;
import com.example.pinokkio.api.order.OrderService;
import com.example.pinokkio.api.pos.dto.response.KioskInfoResponse;
import com.example.pinokkio.api.pos.dto.response.KioskRegisterResponse;
import com.example.pinokkio.api.pos.dto.response.PosResponse;
import com.example.pinokkio.api.pos.dto.response.PosStatisticsResponse;
import com.example.pinokkio.config.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pos")
@Tag(name = "Pos Controller", description = "포스 정보 관련 API")
public class PosController {

    private final PosService posService;
    private final JwtProvider jwtProvider;
    private final OrderService orderService;

    /**
     * 요청한 포스의 자기 정보 조회
     * [가맹점 이름, 포스 ID, 포스 이메일]
     */
    @Operation(summary = "포스 본인정보 조회", description = "포스 본인정보 조회")
    @PreAuthorize("hasRole('ROLE_POS')")
    @GetMapping("/my-info")
    public ResponseEntity<?> getPos() {
        PosResponse posResponse = posService.getMyPosInfo(jwtProvider.getCurrentUserEmail());
        return ResponseEntity.ok(posResponse);
    }

    /**
     * 요청 이메일의 중복 여부 검증
     */
    @Operation(summary = "중복 이메일 조회", description = "포스 내 중복 이메일 조회")
    @GetMapping("/duplicate/{email}")
    public ResponseEntity<?> checkEmailDuplication(@PathVariable String email) {
        EmailDuplicationResponse posResponse = new EmailDuplicationResponse(posService.isEmailDuplicated(email));
        return ResponseEntity.ok(posResponse);
    }

    /**
     * 현재 posId를 기준으로 가지는 code를 포함하는 포스들의 30일 매출 평균, 포스 수, 등수를 반환한다.
     */
    @Operation(summary = "포스 통계 조회", description = "현재 포스의 통계 조회 (30일 매출 평균, 포스 수, 등수)")
    @PreAuthorize("hasRole('ROLE_POS')")
    @GetMapping("/statistics/")
    public ResponseEntity<?> getPosStatistics() {
        UUID posId = posService.getPosByEmail(jwtProvider.getCurrentUserEmail()).getId();
        PosStatisticsResponse statistics = orderService.getPosStatistics(posId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "포스의 키오스크 정보 조회", description = "현재 포스에 연결된 모든 키오스크의 ID, 이메일, 비밀번호 조회")
    @PreAuthorize("hasRole('ROLE_POS')")
    @GetMapping("/kiosks")
    public ResponseEntity<?> getPosKiosks() {
        UUID posId = posService.getPosByEmail(jwtProvider.getCurrentUserEmail()).getId();
        List<KioskInfoResponse> kioskInfos = posService.getKioskInfosByPosId(posId);
        return ResponseEntity.ok(kioskInfos);
    }

    @Operation(summary = "키오스크 등록", description = "POS 사용자가 키오스크를 등록")
    @PreAuthorize("hasRole('ROLE_POS')")
    @PostMapping("/kiosks/register")
    public ResponseEntity<?> registerKiosk() {
        KioskRegisterResponse kioskRegisterResponse = posService.registerKiosk();
        return ResponseEntity.ok(kioskRegisterResponse);
    }

    /**
     * 키오스크 삭제
     * @return ResponseEntity
     */
    @Operation(summary = "키오스크 삭제", description = "POS 사용자가 키오스크를 삭제")
    @PreAuthorize("hasRole('ROLE_POS')")
    @DeleteMapping("/kiosks")
    public ResponseEntity<?> deleteKiosk(@RequestParam UUID kioskId) {
        posService.deleteKiosk(kioskId);
        return ResponseEntity.noContent().build();

    }

}
