package com.example.pinokkio.api.pos;

import com.example.pinokkio.api.kiosk.Kiosk;
import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.pos.dto.response.KioskRegisterResponse;
import com.example.pinokkio.api.pos.dto.response.KioskInfoResponse;
import com.example.pinokkio.api.pos.dto.response.PosResponse;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.exception.domain.kiosk.KioskNotFoundException;
import com.example.pinokkio.exception.domain.pos.PosEmailNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class PosService {

    private final PasswordEncoder passwordEncoder;

    //랜덤 ID 생성기
    private static final String DIGITS = "0123456789";
    private static final Random RANDOM = new SecureRandom();

    private final PosRepository posRepository;
    private final KioskRepository kioskRepository;
    private final UserService userService;

    public PosResponse getMyPosInfo(String email) {
        Pos pos = posRepository
                .findByEmail(email)
                .orElseThrow(() -> new PosEmailNotFoundException(email));
        return new PosResponse(pos.getCode().getName(), pos.getId().toString(), pos.getEmail());
    }

    public Pos getPosByEmail(String email) {
        return posRepository
                .findByEmail(email)
                .orElseThrow(() -> new PosEmailNotFoundException(email));
    }

    public boolean isEmailDuplicated(String email) {
        return posRepository.existsByEmail(email);
    }

    public List<KioskInfoResponse> getKioskInfosByPosId(UUID posId) {
        List<Kiosk> kiosks = kioskRepository.findAllByPosIdOrderByCreatedDateAsc(posId);
        return kiosks.stream()
                .map(kiosk -> new KioskInfoResponse(kiosk.getId(), kiosk.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * email = 코드 이름 + 숫자 4자리 @도메인.com
     * password = 숫자 4자리
     */
    @Transactional
    public KioskRegisterResponse registerKiosk() {
        Pos findPos = userService.getCurrentPos();

        String randomEmail = randomEmail(findPos);
        String randomPassword = randomPassword();

        Kiosk kiosk = Kiosk.builder()
                .pos(findPos)
                .email(randomEmail)
                .password(passwordEncode(randomPassword))
                .build();
        kioskRepository.save(kiosk);

        return new KioskRegisterResponse(kiosk.getId(), findPos.getId(), kiosk.getEmail(), randomPassword);
    }

    /**
     * 입력받은 포스의 키오스크 아이디를 랜덤으로 생성한다.
     *
     * @param pos 키오스크의 출처 포스
     * @return 랜덤 생성된 키오스크 이메일
     */
    public String randomEmail(Pos pos) {

        String brandName = pos.getCode().getName();

        StringBuilder sb = new StringBuilder();
        String newEmail;

        do {
            sb.setLength(0); // StringBuilder 초기화
            sb.append("kiosk");
            for (int i = 0; i < 4; i++) {
                int index = RANDOM.nextInt(DIGITS.length());
                sb.append(DIGITS.charAt(index));
            }
            sb.append("@").append(brandName).append(".com");
            newEmail = sb.toString();
        } while (kioskRepository.existsByEmail(newEmail));

        return newEmail;
    }

    /**
     * 숫자 4자리 비밀번호를 랜덤 생성한다.
     *
     * @return 숫자 4자리 비밀번호
     */
    public String randomPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int index = RANDOM.nextInt(DIGITS.length());
            sb.append(DIGITS.charAt(index));
        }
        return sb.toString();
    }

    /**
     * BCryptPasswordEncoder 로 비밀번호를 암호화한다.
     *
     * @param password 비밀번호
     * @return 암호화된 비밀번호
     */
    public String passwordEncode(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 키오스크 삭제
     *
     * @param kioskId 키오스크 ID
     */
    @Transactional
    public void deleteKiosk(UUID kioskId) {
        log.info("[deleteKiosk] 키오스크 삭제 요청: 키오스크 ID = {}", kioskId);

        Pos currentPos = userService.getCurrentPos();
        Kiosk kiosk = kioskRepository
                .findByIdAndPosId(kioskId, currentPos.getId())
                .orElseThrow(() -> new KioskNotFoundException(kioskId));
        kioskRepository.delete(kiosk);
        log.info("[deleteKiosk] 키오스크 삭제 완료. ID = {}", kioskId);
    }
}
