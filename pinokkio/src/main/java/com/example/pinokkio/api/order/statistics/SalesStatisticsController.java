package com.example.pinokkio.api.order.statistics;

import com.example.pinokkio.api.order.statistics.dto.response.GroupSalesStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sales-statistics")
@RequiredArgsConstructor
@Tag(name = "SalesStatistics Controller", description = "매출 통계 관련 API")
public class SalesStatisticsController {

    private final SalesStatisticsService salesStatisticsService;

    @Operation(summary = "포스별 매출 통계 조회", description = "현재 포스의 일/주/월/년별 총매출액을 제공합니다.")
    @PreAuthorize("hasRole('ROLE_POS')")
    @GetMapping
    public GroupSalesStatisticsResponse getSalesStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return salesStatisticsService.getSalesStatistics(startDate, endDate);
    }

    @Operation(summary = "매출 데이터 백필", description = "지정된 기간의 매출 데이터를 백필합니다.")
    @PreAuthorize("hasRole('ROLE_POS')")
    @PostMapping("/backfill")
    public void backfillSalesData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        salesStatisticsService.backfillSalesStatistics(startDate, endDate);
    }
}