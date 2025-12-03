package com.datanumia.energyconsumption.s2.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;

@Table(name = "daily_consumption")
@Entity
@Immutable  // Senior MUST critiquer : projection ≠ entité mutable
public class DailyConso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String podId;  // POD123

    @Column(nullable = false, updatable = false)
    private LocalDate date;  // 2025-12-03

    @Column(nullable = false)
    private double totalKwhElec;  // 125.50

    @Column(nullable = false)
    private double totalKwhGaz;   // 89.25

    @Column(nullable = false)
    private double totalKwh;      // totalElec + totalGaz

    @Column(nullable = false)
    private int eventCount;       // Nb relevés agrégés

    @Column(nullable = false)
    private boolean hasAnomaly;   // Détection anomalie (>20%)

    // Constructeur pour projections (senior MUST proposer builder)
    public DailyConso(String podId, LocalDate date) {
        this.podId = podId;
        this.date = date;
        this.totalKwhElec = 0.0;
        this.totalKwhGaz = 0.0;
        this.totalKwh = 0.0;
        this.eventCount = 0;
        this.hasAnomaly = false;
    }

    public DailyConso() {

    }

    // Fluent API pour projections (piège senior)
    public DailyConso addElec(double kwh) {
        this.totalKwhElec += kwh;
        this.totalKwh = this.totalKwhElec + this.totalKwhGaz;
        this.eventCount++;
        return this;
    }

    public DailyConso addGaz(double kwh) {
        this.totalKwhGaz += kwh;
        this.totalKwh = this.totalKwhElec + this.totalKwhGaz;
        this.eventCount++;
        return this;
    }

    // Senior MUST critiquer : logique métier dans Read Model
    public boolean exceedsThreshold(double dailyThreshold) {
        return totalKwh > dailyThreshold;
    }

    // Getters only (immutable)
    public String getPodId() { return podId; }
    public LocalDate getDate() { return date; }
    public double getTotalKwhElec() { return totalKwhElec; }
    public double getTotalKwhGaz() { return totalKwhGaz; }
    public double getTotalKwh() { return totalKwh; }
    public int getEventCount() { return eventCount; }
    public boolean hasAnomaly() { return hasAnomaly; }
}

