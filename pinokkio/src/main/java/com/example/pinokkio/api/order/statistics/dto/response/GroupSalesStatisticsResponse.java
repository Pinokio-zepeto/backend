package com.example.pinokkio.api.order.statistics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupSalesStatisticsResponse {
    private List<SalesStatisticsResponse> dailySales;
    private List<SalesStatisticsResponse> weeklySales;
    private List<SalesStatisticsResponse> monthlySales;
    private List<SalesStatisticsResponse> yearlySales;
}
