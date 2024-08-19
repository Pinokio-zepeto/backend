package com.example.pinokkio.api.room;

import com.example.pinokkio.api.room.dto.request.RoomEnterRequest;
import com.example.pinokkio.api.room.dto.response.KioskRoomResponse;
import com.example.pinokkio.api.room.dto.response.RoomResponse;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/meeting")
@Tag(name = "Room Controller", description = "화상 상담 관련 API")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "상담 생성", description = "Teller의 경우 상담방 생성 가능")
    @PreAuthorize("hasRole('ROLE_TELLER')")
    @GetMapping("/teller/room")
    public ResponseEntity<?> createRoom() {
        RoomResponse roomResponse = roomService.createRoom();
        return ResponseEntity.ok(roomResponse);
    }

    @Operation(summary = "상담 삭제", description = "Teller의 경우 상담방 삭제 가능")
    @PreAuthorize("hasRole('ROLE_TELLER')")
    @DeleteMapping("/teller/room")
    public ResponseEntity<?> deleteRoom() {
        roomService.deleteRoom();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상담 요청 수락", description = "Teller가 상담 요청을 수락하는 경우 키오스크에 Room ID를 전송")
    @PreAuthorize("hasRole('ROLE_TELLER')")
    @PostMapping("/teller/accept")
    public ResponseEntity<RoomResponse> acceptInvitation(
            @Validated @RequestBody RoomEnterRequest enterRequest) {
        roomService.acceptInvitation(enterRequest.getRoomId(), enterRequest.getKioskId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상담 요청 거부", description = "Teller가 상담 요청을 거부하는 경우")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    @PostMapping("/kiosk/reject")
    public ResponseEntity<RoomResponse> rejectInvitation() {
        roomService.rejectInvitation();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상담 요청", description = "Kiosk의 경우 생성된 모든 상담방에 상담 요청 전송 가능")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    @PostMapping("/kiosk/request-enter")
    public ResponseEntity<RoomResponse> requestEnterRoom() {
        roomService.sendRequestToAllActiveTellers();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "상담 입장", description = "Kiosk가 화상 상담에 입장")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    @PutMapping("/kiosk/enter")
    public ResponseEntity<?> enterRoom(
            @Validated @RequestBody RoomEnterRequest enterRequest) throws OpenViduJavaClientException, OpenViduHttpException {
        KioskRoomResponse roomResponse = roomService.enterRoom(enterRequest.getRoomId(), enterRequest.getKioskId());
        return ResponseEntity.ok(roomResponse);
    }

    @Operation(summary = "상담 퇴장", description = "Kiosk가 화상 상담에서 퇴장")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    @PutMapping("/kiosk/{roomId}/leave")
    public ResponseEntity<RoomResponse> leaveRoom(
            @Parameter(description = "Room ID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String roomId) {
        roomService.leaveRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
