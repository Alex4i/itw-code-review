package com.datanumia.energyconsumption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumptions")
public class EnergyConsumptionController {

    @Autowired
    private EnergyConsumptionService service;

    @Autowired
    private EnergyConsumptionRepository repository;

    @PostMapping
    public ResponseEntity<String> saveConsumption(@RequestBody Map<String, Object> request) {
        try {
            String podId = (String) request.get("podId");
            Double valueKwh = Double.valueOf(request.get("valueKwh").toString());
            String timestampStr = (String) request.get("timestamp");

            if (valueKwh > 1000000) {
                return ResponseEntity.badRequest().body("Valeur trop élevée");
            }

            EnergyConsumptionEntity entity = new EnergyConsumptionEntity();
            entity.setPodId(podId);
            entity.setValueKwh(valueKwh);
            entity.setTimestamp(Instant.parse(timestampStr));

            service.saveConsumption(entity);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.out.println("Erreur save: " + e.getMessage());
            return ResponseEntity.ok("Erreur interne");
        }
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getTotalConsumption(
            @RequestParam String podId,
            @RequestParam String from,
            @RequestParam String to) {

        Instant fromInstant = Instant.parse(from);
        Instant toInstant = Instant.parse(to);

        List<EnergyConsumptionEntity> consumptions = repository
                .findByPodIdAndTimestampBetween(podId, fromInstant, toInstant);

        double total = 0;
        for (EnergyConsumptionEntity c : consumptions) {
            total += c.getValueKwh();
        }

        return ResponseEntity.ok(total);
    }
}

