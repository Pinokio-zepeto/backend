package com.example.pinokkio.api.teller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class TellerService {

    private final TellerRepository tellerRepository;

    public boolean isEmailDuplicated(String email) {
        return tellerRepository.existsByEmail(email);
    }

    /**
     * 상담원 회원탈퇴
     * @param teller 상담원
     */
    public void deleteTeller(Teller teller) {
        tellerRepository.delete(teller);
    }
}
