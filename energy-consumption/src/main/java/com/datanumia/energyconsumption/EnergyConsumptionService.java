package com.datanumia.energyconsumption;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class EnergyConsumptionService {

    @Autowired
    private EnergyConsumptionRepository repository;

    public void saveConsumption(EnergyConsumptionEntity entity) {
        System.out.println("Sauvegarde consommation: " + entity.getPodId() + " = " + entity.getValueKwh());

        // Contrôle métier "en dur"
        if (entity.getValueKwh() == null || entity.getValueKwh() < 0) {
            throw new RuntimeException("Consommation invalide");
        }

        // On sauvegarde directement
        repository.save(entity);

        log.info("Consommation sauvegardée avec succès pour POD {}", entity.getPodId());
    }

    public double getTotalConsumption(String podId, Instant from, Instant to) {
        List<EnergyConsumptionEntity> consumptions =
                repository.findByPodIdAndTimestampBetween(podId, from, to);

        double total = 0.0;
        for (EnergyConsumptionEntity consumption : consumptions) {
            if (consumption.getValueKwh() != null) {
                total += consumption.getValueKwh();
            }
        }

        // Log métier dans le service (?!)
        log.info("Total consommation pour {} entre {} et {} = {}",
                podId, from, to, total);

        return total;
    }
}

