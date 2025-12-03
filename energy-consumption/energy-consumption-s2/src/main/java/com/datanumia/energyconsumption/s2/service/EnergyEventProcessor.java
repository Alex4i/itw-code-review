package com.datanumia.energyconsumption.s2.service;

import com.datanumia.energyconsumption.s2.domain.DailyConso;
import com.datanumia.energyconsumption.s2.domain.EnergyConsumptionEvent;
import com.datanumia.energyconsumption.s2.repository.DailyConsumptionProjectionRepository;
import com.datanumia.energyconsumption.s2.repository.EnergyEventStore;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnergyEventProcessor {

    private final EnergyEventStore eventStore;
    private final DailyConsumptionProjectionRepository dailyRepo;

    /**
     * Process batch IoT events (1M+/h) → Event Sourcing + Projections
     * PIÈGE SENIOR : N+1 + pas de batch upsert
     */
    public CompletableFuture<Void> processAsync(List<EnergyConsumptionEvent> events) {
        log.info("Processing {} events", events.size());

        // 1. Event Sourcing : append immutable events (AUDIT parfait)
        eventStore.append(events);

        // 2. CQRS Projections (PIÈGE MASSIF N+1)
        return CompletableFuture.runAsync(() -> projectDailyConsos(events))
                .thenRun(() -> log.info("Events processed successfully"));
    }

    /**
     * Projections journalières : Events → Read Models
     * PIÈGE 1 : N+1 findByPodIdAndDate
     * PIÈGE 2 : Mutable DailyConso dans transaction
     * PIÈGE 3 : Pas de batch upsert
     */
    private void projectDailyConsos(List<EnergyConsumptionEvent> events) {
        // Group by POD + Date (senior OK, mais lent sur 1M events)
        Map<String, Map<LocalDate, List<EnergyConsumptionEvent>>> grouped =
                events.stream()
                        .collect(Collectors.groupingBy(
                                EnergyConsumptionEvent::getPodId,
                                Collectors.groupingBy(e -> LocalDate.ofInstant(e.getTimestamp(), ZoneId.systemDefault()))
                        ));

        // N+1 MASSIF (senior MUST proposer Kafka Streams + batch upsert)
        grouped.forEach((podId, dateMap) -> {
            dateMap.forEach((date, dayEvents) -> {
                // PIÈGE : findByPodIdAndDate → N+1
                dailyRepo.findByPodIdAndDate(podId, date)
                        .ifPresentOrElse(
                                existing -> {
                                    // PIÈGE : mutable read model
                                    updateDaily(existing, dayEvents);
                                    dailyRepo.save(existing);  // N+1 save !
                                },
                                () -> {
                                    DailyConso newDaily = buildDaily(podId, date, dayEvents);
                                    dailyRepo.save(newDaily);
                                }
                        );
            });
        });
    }

    /**
     * PIÈGE : logique métier dans processor (→ Domain Service)
     */
    private void updateDaily(DailyConso daily, List<EnergyConsumptionEvent> events) {
        events.forEach(event -> {
            if ("ELEC".equals(event.getMeterType())) {
                daily.addElec(event.getValueKwh());
            } else if ("GAZ".equals(event.getMeterType())) {
                daily.addGaz(event.getValueKwh());
            }
        });
    }

    private DailyConso buildDaily(String podId, LocalDate date, List<EnergyConsumptionEvent> events) {
        DailyConso daily = new DailyConso(podId, date);
        events.forEach(event -> {
            if ("ELEC".equals(event.getMeterType())) {
                daily.addElec(event.getValueKwh());
            } else {
                daily.addGaz(event.getValueKwh());
            }
        });
        return daily;
    }
}


