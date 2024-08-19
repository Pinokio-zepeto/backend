package com.example.pinokkio.api.pos.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosResponse {
    private String codeName;
    private String posId;
    private String posEmail;
    public PosResponse(String codeName, String posId, String posEmail) {
        this.codeName = codeName;
        this.posId = posId;
        this.posEmail = posEmail;
    }
}
