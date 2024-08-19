package com.example.pinokkio.api.user;

import com.example.pinokkio.api.kiosk.Kiosk;
import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.api.pos.PosRepository;
import com.example.pinokkio.api.teller.Teller;
import com.example.pinokkio.api.teller.TellerRepository;
import com.example.pinokkio.config.jwt.JwtProvider;
import com.example.pinokkio.exception.base.AuthenticationException;
import com.example.pinokkio.exception.base.AuthorizationException;
import com.example.pinokkio.exception.domain.kiosk.InvalidPosException;
import com.example.pinokkio.exception.domain.kiosk.KioskNotFoundException;
import com.example.pinokkio.exception.domain.pos.PosNotFoundException;
import com.example.pinokkio.exception.domain.teller.TellerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final JwtProvider jwtProvider;
    private final PosRepository posRepository;
    private final TellerRepository tellerRepository;
    private final KioskRepository kioskRepository;


    /**
     * 토큰으로부터 현재 엔티티를 반환하는 메서드
     */
    public Object getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.info("here::::: authentication: {}" , authentication);
            throw new AuthenticationException("AUTH_001", "User is not authenticated");
        }

        String userEmail = jwtProvider.getCurrentUserEmail();
        String userRole = jwtProvider.getRoleFromToken(authentication.getCredentials().toString());

        return switch (userRole) {
            case "ROLE_POS" -> posRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new PosNotFoundException(userEmail));
            case "ROLE_KIOSK" -> kioskRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new KioskNotFoundException(userEmail));
            case "ROLE_TELLER" -> tellerRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new TellerNotFoundException(userEmail));
            default -> throw new AuthorizationException("AUTH_003", "Unknown user role: " + userRole);
        };
    }

    public Pos getCurrentPos() {
        Object user = getCurrentUser();
        if (!(user instanceof Pos)) {
            throw new AuthorizationException("AUTH_004", "Current user is not a POS user");
        }
        return (Pos) user;
    }

    public Kiosk getCurrentKiosk() {
        Object user = getCurrentUser();
        if (!(user instanceof Kiosk)) {
            throw new AuthorizationException("AUTH_004", "Current user is not a Kiosk user");
        }
        return (Kiosk) user;
    }

    public Teller getCurrentTeller() {
        Object user = getCurrentUser();
        if (!(user instanceof Teller)) {
            throw new AuthorizationException("AUTH_004", "Current user is not a Teller user");
        }
        return (Teller) user;
    }

    public UUID getCurrentPosId() {
        Object currentUser = getCurrentUser();
        if (currentUser instanceof Pos) {
            return ((Pos) currentUser).getId();
        }

        if (currentUser instanceof Kiosk) {
            return Optional.ofNullable(((Kiosk) currentUser).getPos())
                    .map(Pos::getId)
                    .orElseThrow(InvalidPosException::new);
        }
        throw new AuthorizationException("AUTH_007", "Current user is neither POS nor Kiosk");
        }
}
