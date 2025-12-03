package com.datanumia.energyconsumption.s2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyConsumptionServiceUnitTest {

    @Mock
    private EnergyConsumptionRepository repository;

    @InjectMocks
    private EnergyConsumptionService service;

    @Test
    public void testGetTotalConsumption() {
        Instant now = Instant.now();
        Instant yda = now.minus(1, ChronoUnit.DAYS);
        EnergyConsumptionEntity entity = new EnergyConsumptionEntity();
        entity.setPodId("POD123");
        entity.setValueKwh(10.5);
        entity.setTimestamp(now);

        when(repository.findByPodIdAndTimestampBetween("POD123", yda, now))
                .thenReturn(List.of(entity));

        double total = service.getTotalConsumption("POD123",
                yda, now);

        assertEquals(10.5, total, 0.01);
        verify(repository).findByPodIdAndTimestampBetween("POD123", yda, now);
    }
}

