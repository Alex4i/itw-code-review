# Tech Interview - Revue de code

## Contexte

**En tant que** particulier abonné Datanumia,
**Je veux** voir ma consommation élec/gaz quotidienne sur mon dashboard,
**Afin de** détecter les dépassements de budget et optimiser mes habitudes.

**AC1** : POST /api/consumptions → enregistre relevés IoT 15min

**AC2** : GET /api/consumptions/total?podId=POD123&from=2025-12-01&to=2025-12-03 → somme kWh

Voici l'implémentation actuelle d'un dev junior.
**Ta mission** : revue de code comme en PR, puis on refactor ensemble tes idées prioritaires.

## Quelques questions d'orientation

* EnergyConsumptionController -> qu'est-ce qui saute aux yeux en 1er ?
* Comment réorganiser les responsabilités entre controller, service, repository ?
* Quelles validations d'entrée sont importantes dans le contexte métier (conso energétique) ?
* Avis sur la gestion des erreurs et codes HTTP ? Quoi et comment améliorer ?
* Quels changements de design pourrait-on faire pour rendre le service plus testable ?
* Comment structurer les tests (unit, itg) pour sécuriser les comportements métiers ?
* Si on utilise ce service en production avec des volumes importants, qu'est-ce qui est inquiétant dans le code ? (perfs, BDD, logs, types de données etc.)

## Quelques points d'évaluation

* Design et architecture : séparation controller/service/domaine/infrastructure, archi hexagonale
* Sens métier : compréhension basique du domaine (conso energétique) et invariants (par exemple : valeurs positives, cohérence temporelle etc.)
* Qualité & tests : parler de validation, tests ciblés, robustesse...
* Prod & maintenance : logs, erreurs, perfs, sans sur complexité

## Réponses attendues

**✅ RÉPONSES CLÉS (mid+)** :
• **Parsing Map manuel** → DTO + @Valid obligatoire
• **Repository injecté dans Controller** → violation SRP, bypass service
• **Toujours 200 OK** même en erreur → codes HTTP faux
• **Try/catch générique** → swallow exceptions, debug impossible
• **System.out.println** → SLF4J logger structuré

**✅ ARCHI MID+** :

#### Controller

* DTO validation
* HTTP mapping
* Exception HTTP
* Logs entrée/sortie

#### Service

* Logique métier
* Validation métier
* Domain mapping (CRUD only)

#### Repository

* findByPodId...
* save(entity)

**✅ VALIDATIONS MID+** :
• **valueKwh >= 0** (conso négative impossible)
• **podId non vide** + format (POD\d{3})
• **timestamp récent** (±24h max)
• **valueKwh < 1_000_000** (plafond physique)
• **Pas de doublon** (podId + timestamp ±1min)

**✅ CODES HTTP MID+** :
POST /consumptions :

201 Created (nouvelle conso)

400 Bad Request (validation KO)

500 Internal Server Error (exception)

Global : @ControllerAdvice + Problem+json

✅ DESIGN TESTABLE MID+ :

DTO → Domain mapping dans service (pas controller)

Repository interface → mock facile

Validation métier extraite → test isolé

Pas de Map parsing → @Valid DTOs

Avant ❌ : Controller = Map parsing + repo direct
Après ✅ : Controller fin → Service testable

✅ PROD MID+ :

**PERFS** :
• N+1 repository dans GET /total → pagination + index
• Boucle Java sum() → SQL SUM natif

**BDD** :
• Pas d'index podId + timestamp → requête full scan
• Double nullable → contrainte CHECK

**LOGS** :
• System.out → SLF4J structuré
• Pas de corrélation podId → debug impossible

**ROBUSTE** :
• Pas de timeout DB → HikariCP
• Pas de validation entrée → injection SQL