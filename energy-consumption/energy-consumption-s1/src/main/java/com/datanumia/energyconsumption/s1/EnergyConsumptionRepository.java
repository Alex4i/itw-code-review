package com.datanumia.energyconsumption.s1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumptionEntity, Long> {

    List<EnergyConsumptionEntity> findByClientIdAndTimestampBetween(String podId, Instant from, Instant to);
}

