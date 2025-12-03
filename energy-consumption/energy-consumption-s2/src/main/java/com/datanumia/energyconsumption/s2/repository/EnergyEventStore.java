package com.datanumia.energyconsumption.s2.repository;

import com.datanumia.energyconsumption.s2.domain.EventStream;
import com.datanumia.energyconsumption.s2.domain.EnergyConsumptionEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EnergyEventStore {
    void append(List<EnergyConsumptionEvent> events);
    EventStream readStream(String podId, Instant from);
    boolean exists(UUID idempotencyKey);
}
