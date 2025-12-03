package com.datanumia.energyconsumption.s1;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "energy_consumption")
public class EnergyConsumptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientId;

    private Double valueKwh;

    private Instant timestamp;

}

