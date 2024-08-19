package com.example.pinokkio.api.order.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesStatisticsRepository extends JpaRepository<SalesStatistics, UUID> {

    List<SalesStatistics> findByPosIdAndPeriodTypeAndYearAndPeriodBetweenOrderByYearAscPeriodAsc(
            UUID posId, PeriodType periodType, int year, int startPeriod, int endPeriod);

    Optional<SalesStatistics> findByPosIdAndPeriodTypeAndYearAndPeriodOrderByYearAscPeriodAsc(
            UUID posId, PeriodType periodType, int year, int period);

    List<SalesStatistics> findByPosIdAndPeriodTypeAndYearBetweenOrderByYearAscPeriodAsc(
            UUID posId, PeriodType periodType, int startYear, int endYear);
}
