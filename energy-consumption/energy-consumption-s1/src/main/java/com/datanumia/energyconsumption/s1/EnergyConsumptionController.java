package com.datanumia.energyconsumption.s1;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consumptions")
public class EnergyConsumptionController {

    static Integer ADVANCED_OPERATION_COUNT = 0;

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
            entity.setClientId(podId);
            entity.setValueKwh(valueKwh);
            entity.setTimestamp(Instant.parse(timestampStr));

            if (request.get("operationType").equals("advanced")){
                ADVANCED_OPERATION_COUNT++;
            } else {
                service.saveConsumption(entity);
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            System.out.println("Erreur save: " + e.getMessage());
            return ResponseEntity.ok("Erreur interne");
        }
    }

    @SneakyThrows
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> saveConsumptionFromCsv(@RequestParam("csvFile") MultipartFile csvFile) {
        InputStream is = csvFile.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        if (csvFile.getName().contains("basic")) {
            reader.lines()
                    .map(a -> {
                        // Format basique : clientId;valueKWh;timestamp
                        String[] split = a.split(";");
                        EnergyConsumptionEntity entity = new EnergyConsumptionEntity();
                        entity.setClientId(split[0]);
                        entity.setValueKwh(Double.parseDouble(split[1]));
                        entity.setTimestamp(Instant.parse(split[2]));
                        return entity;
                    }).forEach(a -> service.saveConsumption(a));
        } else if (csvFile.getName().contains("advanced")) {
            reader.lines()
                    .map(a -> {
                        // Format avancé : clientId;power;consumption_duration;minutes_before;operation_type
                        String[] split = a.split(";");
                        EnergyConsumptionEntity entity = new EnergyConsumptionEntity();
                        entity.setClientId(split[0]);
                        entity.setValueKwh(Double.parseDouble(split[1]) * Double.parseDouble(split[2]));
                        entity.setTimestamp(Instant.now().minus(Duration.of(Long.parseLong(split[3]), ChronoUnit.MINUTES)));
                        if (split[3].equals("SAVE")) {
                            service.saveConsumption(entity);
                        } else if (split[3].equals("COUNT")) {
                            ADVANCED_OPERATION_COUNT = ADVANCED_OPERATION_COUNT + 1;
                        }
                        return entity;
                    }).toList();
        }
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getTotalConsumption(
            @RequestParam String podId,
            @RequestParam String from,
            @RequestParam String to) {

        Instant fromInstant = Instant.parse(from);
        Instant toInstant = Instant.parse(to);

        List<EnergyConsumptionEntity> consumptions = repository
                .findByClientIdAndTimestampBetween(podId, fromInstant, toInstant);

        double total = 0;
        for (EnergyConsumptionEntity c : consumptions) {
            total += c.getValueKwh();
        }

        return ResponseEntity.ok(total);
    }
}

