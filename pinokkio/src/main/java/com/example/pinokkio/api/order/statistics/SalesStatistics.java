package com.example.pinokkio.api.order.statistics;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "SalesStatistics")
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class SalesStatistics {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", name = "sales_statistics_id")
    private UUID id;

    @Column(nullable = false)
    private UUID posId;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType periodType;

    @Column(nullable = false)
    private long totalSales;

    public SalesStatistics(UUID posId, int year, int period, PeriodType periodType, long totalSales) {
        this.posId = posId;
        this.year = year;
        this.period = period;
        this.periodType = periodType;
        this.totalSales = totalSales;
    }
}
