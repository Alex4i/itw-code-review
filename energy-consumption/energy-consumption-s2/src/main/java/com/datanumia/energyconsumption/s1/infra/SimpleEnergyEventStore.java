package com.datanumia.energyconsumption.s1.infra;

import com.datanumia.energyconsumption.s1.domain.EnergyConsumptionEvent;
import com.datanumia.energyconsumption.s1.domain.EventStream;
import com.datanumia.energyconsumption.s1.repository.EnergyEventStore;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SimpleEnergyEventStore implements EnergyEventStore {

    private final Map<String, List<EnergyConsumptionEvent>> store = new ConcurrentHashMap<>();
    private final Set<UUID> idempotencyKeys = ConcurrentHashMap.newKeySet();

    @Override
    public void append(List<EnergyConsumptionEvent> events) {
        events.forEach(event ->
                store.computeIfAbsent(event.getPodId(), k -> Collections.synchronizedList(new ArrayList<>()))
                        .add(event));
    }

    @Override
    public EventStream readStream(String podId, Instant from) {
        List<EnergyConsumptionEvent> allEvents = store.getOrDefault(podId, Collections.emptyList());
        List<EnergyConsumptionEvent> filtered = allEvents.stream()
                .filter(e -> e.getTimestamp().isAfter(from))
                .toList();

        return new EventStream(filtered, from, Instant.now());
    }

    @Override
    public boolean exists(UUID idempotencyKey) {
        return idempotencyKeys.contains(idempotencyKey);
    }
}

