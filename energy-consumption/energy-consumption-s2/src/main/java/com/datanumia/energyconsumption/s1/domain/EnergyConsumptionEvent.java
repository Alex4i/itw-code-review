package com.datanumia.energyconsumption.s1.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

// Événement immuable (senior MUST critiquer JPA CRUD)
@Data
@Builder
public class EnergyConsumptionEvent {
    String podId;
    double valueKwh;
    Instant timestamp;
    String meterType; // "ELEC", "GAZ"
    String source;    // "MQTT", "API", "MANUAL"

}

