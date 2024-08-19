package com.example.pinokkio.api.teller;

import com.example.pinokkio.api.auth.dto.response.EmailDuplicationResponse;
import com.example.pinokkio.api.pos.dto.response.PosResponse;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.config.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teller")
@Tag(name = "Teller Controller", description = "상담원 정보 관련 API")
public class TellerController {

    private final TellerService tellerService;
    private final UserService userService;

    @Operation(summary = "중복 이메일 조회", description = "포스 내 중복 이메일 조회")
    @GetMapping("/duplicate/{email}")
    public ResponseEntity<?> checkEmailDuplication(@PathVariable String email) {
        EmailDuplicationResponse posResponse = new EmailDuplicationResponse(tellerService.isEmailDuplicated(email));
        return ResponseEntity.ok(posResponse);
    }

    @Operation(summary = "상담원 회원탈퇴", description = "상담원 회원탈퇴 요청")
    @PreAuthorize("hasRole('ROLE_TELLER')")
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTeller() {
        tellerService.deleteTeller(userService.getCurrentTeller());
        return ResponseEntity.noContent().build();
    }

}
