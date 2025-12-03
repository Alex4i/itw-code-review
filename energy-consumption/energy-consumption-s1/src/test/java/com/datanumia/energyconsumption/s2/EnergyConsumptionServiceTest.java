package com.datanumia.energyconsumption.s2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class EnergyConsumptionServiceTest {

    @MockBean
    private EnergyConsumptionRepository repository;

    @Autowired
    private EnergyConsumptionService service;

    @Test
    void testGetTotalConsumption() {
        EnergyConsumptionEntity entity = new EnergyConsumptionEntity();
        entity.setPodId("POD123");
        entity.setValueKwh(10.5);
        entity.setTimestamp(Instant.now());

        when(repository.findByPodIdAndTimestampBetween(any(), any(), any()))
                .thenReturn(List.of(entity));

        double total = service.getTotalConsumption("POD123", Instant.now().minus(1, ChronoUnit.DAYS), Instant.now());

        assertEquals(10.5, total, 0.01);
    }
}

