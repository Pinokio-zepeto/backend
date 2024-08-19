package com.example.pinokkio.api.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KioskInfoResponse {
    private UUID kioskId;
    private String email;
}