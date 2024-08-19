package com.example.pinokkio.api.order.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesStatisticsResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalSales;
}
