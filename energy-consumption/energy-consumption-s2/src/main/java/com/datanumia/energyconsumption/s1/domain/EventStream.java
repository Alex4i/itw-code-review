package com.datanumia.energyconsumption.s1.domain;

import java.time.Instant;
import java.util.List;

public record EventStream(List<EnergyConsumptionEvent> events, Instant from, Instant to) {}
