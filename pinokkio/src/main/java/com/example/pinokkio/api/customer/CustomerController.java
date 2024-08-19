package com.example.pinokkio.api.customer;

import com.example.pinokkio.api.customer.dto.request.CustomerRegistrationEvent;
import com.example.pinokkio.api.customer.dto.request.CustomerRegistrationRequest;
import com.example.pinokkio.api.customer.dto.response.CustomerResponse;
import com.example.pinokkio.api.customer.sse.SSEService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Base64;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Controller", description = "고객 관련 API")
public class CustomerController {

    private final CustomerService customerService;
    private final SSEService sseService;

    @Operation(summary = "신규 고객 등록", description = "특정 포스에 신규 고객을 등록")
    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    public ResponseEntity<?> register(@RequestBody CustomerRegistrationRequest request) {
        log.info("Received registration request: {}", request);
        CustomerResponse newCustomer = customerService.saveCustomer(request.getAnalysisResult(), request.getPhoneNumber());
        return ResponseEntity.ok(newCustomer);
    }

    @Operation(summary = "전화번호를 이용한 고객 조회", description = "전화번호로 특정 포스 내의 고객 정보를 조회")
    @GetMapping("/phone-number")
    @PreAuthorize("hasRole('ROLE_KIOSK')")
    public ResponseEntity<?> register(@RequestParam String phoneNumber) {
        log.info("[고객 정보 조회] phoneNumber: {}", phoneNumber);
        CustomerResponse findCustomer = customerService.findCustomerByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(findCustomer);

    }

    @GetMapping("/face-recognition-events")
    public SseEmitter subscribeToEvents() {
        return sseService.createEmitter();
    }

    // CustomerRegistrationEvent를 처리하는 이벤트 리스너 메서드입니다.
    @EventListener
    public void handleCustomerRegistrationEvent(CustomerRegistrationEvent event) {
        // 이벤트에 포함된 요청 정보로 고객 등록을 수행합니다.
        register(event.getRequest());
    }
}
