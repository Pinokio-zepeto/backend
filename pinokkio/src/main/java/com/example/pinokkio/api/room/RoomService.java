package com.example.pinokkio.api.room;

import com.example.pinokkio.api.kiosk.Kiosk;
import com.example.pinokkio.api.kiosk.KioskRepository;
import com.example.pinokkio.api.room.dto.response.KioskRoomResponse;
import com.example.pinokkio.api.room.dto.response.RoomResponse;
import com.example.pinokkio.api.teller.Teller;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.common.utils.EntityUtils;
import com.example.pinokkio.exception.domain.kiosk.KioskNotFoundException;
import com.example.pinokkio.exception.domain.room.RoomAccessRestrictedException;
import com.example.pinokkio.exception.domain.room.RoomNotAvailableException;
import com.example.pinokkio.exception.domain.room.RoomNotFoundException;
import com.example.pinokkio.exception.domain.room.TokenCreateFailException;
import io.openvidu.java.client.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private static final int MAX_CAPACITY = 3;
    private static final String KIOSK_ROLE = "KIOSK";
    private static final String TELLER_ROLE = "TELLER";

    private OpenVidu openvidu;

    private final RoomRepository roomRepository;
    private final KioskRepository kioskRepository;
    private final WebSocketService webSocketService;
    private final UserService userService;

    private final ReentrantLock roomLock = new ReentrantLock();

    @Value("${openvidu.url}")
    public String OPENVIDU_URL;

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    @Transactional
    public RoomResponse createRoom() {
        Teller teller = userService.getCurrentTeller();

        return roomRepository.findByTeller(teller)
                .map(room -> createRoomResponseForExistingRoom(room, teller))
                .orElseGet(() -> createRoomResponseForNewRoom(teller));
    }

    private RoomResponse createRoomResponseForExistingRoom(Room room, Teller teller) {
        try {
            String token = createToken(room.getRoomId(), teller.getId(), TELLER_ROLE);
            return new RoomResponse(room.getRoomId().toString(), token);
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("Error creating token for existing room: {}", room.getRoomId(), e);
            throw new RoomAccessRestrictedException("Failed to create token for existing room");
        }
    }

    @Transactional
    public RoomResponse createRoomResponseForNewRoom(Teller teller) {
        Room newRoom = Room.builder()
                .teller(teller)
                .numberOfCustomers(0)
                .build();
        roomRepository.save(newRoom);

        try {
            SessionProperties properties = new SessionProperties.Builder()
                    .customSessionId(newRoom.getRoomId().toString())
                    .build();
            Session session = openvidu.createSession(properties);
            String token = createToken(UUID.fromString(session.getSessionId()), teller.getId(), TELLER_ROLE);
            return new RoomResponse(session.getSessionId(), token);
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("Error creating new room for teller: {}", teller.getId(), e);
            throw new TokenCreateFailException();
        }
    }

    @Transactional
    public void deleteRoom() {
        Teller teller = userService.getCurrentTeller();
        roomRepository.deleteByTeller(teller);
        log.info("Room deleted for teller: {}", teller.getId());
    }

    public void acceptInvitation(UUID roomId, UUID kioskId) {
        if (webSocketService.isTokenIssued(kioskId)) {
            throw new RoomAccessRestrictedException("Token already issued for kiosk: " + kioskId);
        }

        Teller teller = userService.getCurrentTeller();
        Room room = roomRepository.findByTeller(teller)
                .orElseThrow(() -> new RoomNotFoundException("Room not found for teller: " + teller.getId()));

        validateRoomId(roomId, room);
        validateKioskId(kioskId);

        webSocketService.sendRoomId(kioskId, room.getRoomId());
        log.info("Invitation accepted for room: {}, kiosk: {}, teller: {}", roomId, kioskId, teller.getId());
    }

    private void validateKioskId(UUID kioskId) {
        EntityUtils.getEntityById(kioskRepository, kioskId, KioskNotFoundException::new);
    }

    private void validateRoomId(UUID roomId, Room room) {
        if (!room.getRoomId().equals(roomId)) {
            throw new RoomNotFoundException("Room not found: " + roomId);
        }
    }

    public void rejectInvitation() {
        Teller teller = userService.getCurrentTeller();
        Room room = roomRepository.findByTeller(teller)
                .orElseThrow(() -> new RoomNotFoundException("Room not found for teller: " + teller.getId()));
        log.info("Invitation rejected for room: {}, teller: {}", room.getRoomId(), teller.getId());
    }

    public void sendRequestToAllActiveTellers() {
        Kiosk curKiosk = userService.getCurrentKiosk();
        List<Session> activeSessions = openvidu.getActiveSessions();

        if (activeSessions.isEmpty()) {
            throw new RoomAccessRestrictedException("No active sessions available");
        }

        for (Session session : activeSessions) {
            processActiveSession(session, curKiosk);
        }

        log.info("Consultation request sent to all active tellers for kiosk: {}", curKiosk.getId());
    }

    private void processActiveSession(Session session, Kiosk kiosk) {
        UUID curRoomId = UUID.fromString(session.getSessionId());
        Room room = roomRepository.findById(curRoomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found: " + curRoomId));

        Teller teller = room.getTeller();
        if (teller != null) {
            sendConsultationRequestToTeller(teller, kiosk, curRoomId.toString());
        } else {
            log.warn("Room {} has no associated teller", curRoomId);
        }
    }

    private void sendConsultationRequestToTeller(Teller teller, Kiosk kiosk, String sessionId) {
        try {
            JSONObject jsonMessage = new JSONObject()
                    .put("type", "consultationRequest")
                    .put("kioskId", kiosk.getId().toString())
                    .put("sessionId", sessionId);

            webSocketService.sendMessage(teller.getId(), new TextMessage(jsonMessage.toString()));
        } catch (JSONException e) {
            log.error("Error creating JSON message for consultation request", e);
        }
    }

    @Transactional
    public KioskRoomResponse enterRoom(UUID roomId, UUID kioskId) throws OpenViduJavaClientException, OpenViduHttpException {
        roomLock.lock();
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RoomNotFoundException("Room not found: " + roomId));

            if (room.getNumberOfCustomers() >= MAX_CAPACITY) {
                throw new RoomNotAvailableException(roomId);
            }

            room.updateNumberOfCustomers(room.getNumberOfCustomers() + 1);
            roomRepository.save(room);

            log.info("Kiosk {} entered room {}", kioskId, roomId);

            String videoToken = createToken(roomId, kioskId, KIOSK_ROLE);
            String screenToken = createToken(roomId, kioskId, KIOSK_ROLE);

            return new KioskRoomResponse(room.getRoomId().toString(), videoToken, screenToken);
        } finally {
            roomLock.unlock();
        }
    }

    @Transactional
    public void leaveRoom(String roomId) {
        roomLock.lock();
        try {
            Room room = roomRepository.findById(UUID.fromString(roomId))
                    .orElseThrow(() -> new RoomNotFoundException("Room not found: " + roomId));

            if (room.getNumberOfCustomers() > 0) {
                room.updateNumberOfCustomers(room.getNumberOfCustomers() - 1);
                roomRepository.save(room);
            }
            log.info("A customer left room: {}, new number of customers: {}", roomId, room.getNumberOfCustomers());
        } finally {
            roomLock.unlock();
        }
    }

    private String createToken(UUID roomId, UUID userId, String userRole) throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openvidu.getActiveSession(roomId.toString());
        if (session == null) {
            throw new RoomNotFoundException("Active session not found: " + roomId);
        }

        JSONObject userData = new JSONObject();
        userData.put("userId", userId);
        userData.put("role", userRole);

        ConnectionProperties properties = new ConnectionProperties.Builder()
                .role(OpenViduRole.valueOf(userRole.equals(KIOSK_ROLE) ? "PUBLISHER" : "MODERATOR"))
                .data(userData.toString())
                .build();
        Connection connection = session.createConnection(properties);
        return connection.getToken();
    }
}