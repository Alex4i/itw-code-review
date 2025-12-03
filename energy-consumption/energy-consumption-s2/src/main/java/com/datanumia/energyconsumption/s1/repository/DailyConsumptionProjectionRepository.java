package com.datanumia.energyconsumption.s1.repository;

import com.datanumia.energyconsumption.s1.domain.DailyConso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyConsumptionProjectionRepository extends JpaRepository<DailyConso, Long> {
    Optional<DailyConso> findByPodIdAndDate(String podId, LocalDate date);

}
