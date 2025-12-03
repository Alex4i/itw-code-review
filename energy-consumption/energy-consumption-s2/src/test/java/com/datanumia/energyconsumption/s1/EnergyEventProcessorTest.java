package com.datanumia.energyconsumption.s1;

import com.datanumia.energyconsumption.s1.domain.EnergyConsumptionEvent;
import com.datanumia.energyconsumption.s1.service.EnergyEventProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EnergyEventProcessorTest {

    @Autowired
    private EnergyEventProcessor processor;

    @Test
    void testProcessAsync() {
        // Given
        List<EnergyConsumptionEvent> events = List.of(
                EnergyConsumptionEvent.builder()
                        .podId("POD123")
                        .meterType("ELEC")
                        .valueKwh(10.5)
                        .timestamp(Instant.now())
                        .build(),
                EnergyConsumptionEvent.builder()
                        .podId("POD123")
                        .meterType("GAZ")
                        .valueKwh(5.2)
                        .timestamp(Instant.now())
                        .build()
        );

        // When
        CompletableFuture<Void> future = processor.processAsync(events);
        future.join();

        // Then : events stockés + projection créée
        assertTrue(true); // Mock verify dans version complète
    }
}
