package com.example.pinokkio.config.jwt;

import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.teller.TellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final PosRepository posRepository;
    private final KioskRepository kioskRepository;
    private final TellerRepository tellerRepository;

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        log.info("입력 값: {}", input);

        // 입력 값에서 역할과 이메일 분리
        String inputRole = input.substring(0, 1);
        String email = input.substring(1);

        log.info("입력 역할: {}", inputRole);
        log.info("입력 이메일: {}", email);

        // 역할을 Enum으로 변환
        Role role;
        try {
            role = Role.valueOf(inputRole);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 역할 입력: {}", inputRole, e);
            throw new UsernameNotFoundException("잘못된 역할 입력: " + inputRole, e);
        }

        log.info("변환된 역할: {}", role);

        // 역할에 따른 사용자 검색 및 반환
        switch (role) {
            case P:
                log.info("POS 사용자 검색 중...");
                return posRepository.findByEmail(email)
                        .map(pos -> {
                            log.info("POS 사용자 발견: {}", pos.getEmail());
                            return new CustomUserDetail(pos.getEmail(), pos.getPassword(), role.getValue());
                        })
                        .orElseThrow(() -> {
                            log.error("{} : POS 사용자 존재하지 않음", email);
                            return new UsernameNotFoundException(email + " : POS 사용자 존재하지 않음");
                        });

            case K:
                log.info("KIOSK 사용자 검색 중...");
                return kioskRepository.findByEmail(email)
                        .map(kiosk -> {
                            log.info("KIOSK 사용자 발견: {}", kiosk.getEmail());
                            return new CustomUserDetail(kiosk.getEmail(), kiosk.getPassword(), role.getValue());
                        })
                        .orElseThrow(() -> {
                            log.error("{} : KIOSK 사용자 존재하지 않음", email);
                            return new UsernameNotFoundException(email + " : KIOSK 사용자 존재하지 않음");
                        });

            case T:
                log.info("TELLER 사용자 검색 중...");
                return tellerRepository.findByEmail(email)
                        .map(teller -> {
                            log.info("TELLER 사용자 발견: {}", teller.getEmail());
                            return new CustomUserDetail(teller.getEmail(), teller.getPassword(), role.getValue());
                        })
                        .orElseThrow(() -> {
                            log.error("{} : TELLER 사용자 존재하지 않음", email);
                            return new UsernameNotFoundException(email + " : TELLER 사용자 존재하지 않음");
                        });

            default:
                log.error("알 수 없는 역할: {}", role);
                throw new UsernameNotFoundException(role + " : 알 수 없는 역할");
        }
    }
}
