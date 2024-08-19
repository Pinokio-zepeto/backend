package com.example.pinokkio.api.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PosStatisticsResponse {
    private long averageSales;
    private long posCount;
    private long currentPosRank;
}
