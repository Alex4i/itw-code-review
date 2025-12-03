package com.datanumia.energyconsumption.s2.controller;

import com.datanumia.energyconsumption.s2.dto.ApiResponse;
import com.datanumia.energyconsumption.s2.domain.EnergyConsumptionEvent;
import com.datanumia.energyconsumption.s2.service.EnergyEventProcessor;
import com.datanumia.energyconsumption.s2.repository.EnergyEventStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/consumptions")
@Validated
@Slf4j
public class EnergyConsumptionController {

    private final EnergyEventStore eventStore;

    private final EnergyEventProcessor eventProcessor;

    public EnergyConsumptionController(EnergyEventProcessor eventProcessor,
                                       EnergyEventStore eventStore) {
        this.eventProcessor = eventProcessor;
        this.eventStore = eventStore;
    }

    @PostMapping("/events")
    public CompletableFuture<ResponseEntity<ApiResponse>> ingestEvents(
            @RequestBody List<EnergyConsumptionEvent> events,
            @RequestHeader(value = "X-Idempotency-Key", required = false) UUID idempotencyKey) {

        // Idempotence manuelle
        if (eventStore.exists(idempotencyKey)) {
            return CompletableFuture.completedFuture(ResponseEntity.ok().build());
        }

        return eventProcessor.processAsync(events)
                .thenApply(result -> ResponseEntity.ok(ApiResponse.ok(true, "Processed")))
                .exceptionally(throwable -> {
                    log.error("Erreur ingestion events", throwable);
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .body(ApiResponse.error("Ingestion failed: " + throwable.getMessage()));
                });
    }

}

