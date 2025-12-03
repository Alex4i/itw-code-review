# Tech Interview - Revue de code

## Contexte

En tant qu’opérateur chez Datanumia,
Je souhaite ingérer et suivre en temps réel les événements de consommation énergétique (électricité, gaz) provenant de millions de compteurs IoT,
Afin de pouvoir calculer les consommations journalières, détecter automatiquement les anomalies et déclencher des alertes en quasi temps réel.

**Feature principale :**

- Un endpoint POST /api/v1/consumptions/events permet d’ingérer par batch des événements bruts issus des compteurs IoT.
- Chaque événement est immuable et représente un relevé précis (podId, timestamp, valeur, type de compteur).
- Les événements doivent être stockés de façon fiable (Event Sourcing), puis transformés en projections agrégées journalières utilisées pour les alertes et dashboards.
- L’ingestion doit garantir l’idempotence, la résilience face à la forte volumétrie (plusieurs millions d’événements par heure), et permettre une observation fine en production (tracing, métriques).

**Objectif de l’exercice :**

Nous allons te présenter un extrait du microservice d’ingestion EnergyConsumption, notamment le processeur d’événements et la gestion des projections.

Ta mission est de faire une revue critique du code existant, d’identifier les points faibles, et de proposer des améliorations pragmatiques et scalables.

Tu peux démarrer ta revue comme en pull request.

## Quelques questions d'orientation

**1. 1ère impression globale (2 min)**
• Qu'est-ce qui te choque IMMÉDIATEMENT dans cette architecture ?
• EventStore interface → quelle implémentation réelle envisages-tu ?

**2. Event Sourcing / CQRS (5 min)**
• Le découpage EventStore vs DailyConso projections : bien pensé ou à revoir ?
• Où placerais-tu la frontière exacte "Write Model" vs "Read Model" ?
• L'EventStore append() → Kafka ? S3 ? Les deux ? Pourquoi ?

**3. Performance / Scalabilité (8 min)**
• 1M events/heure : le projectDailyConsos() tient-il la route ? Pourquoi ?
• Le groupBy + N+1 findByPodIdAndDate → quelle refactorisation prioritaire ?
• Projections synchrone → asynchrone ? Kafka Streams ? Flink ?

**4. Idempotence / Résilience (5 min)**
• L'idempotencyKey header → suffisant ou Kafka exactly-once préférable ?
• Où mets-tu la Dead Letter Queue ? Retry policy ? Circuit Breaker ?
• Comment garantis-tu "exactly-once" sur l'ensemble de la chaîne ?

**5. Observabilité / Production (5 min)**
• Quelles métriques DORA impératives pour ce processor (lead time, MTTR) ?
• Tracing distribué : comment traces-tu un event de MQTT → projection → alerte ?
• Logs actuels : structurés ? Corrélation ? Niveaux ?

**6. Testabilité / Architecture (5 min)**
• Comment testes-tu l'end-to-end (IoT → projection → alerte) en CI/CD ?
• DailyConso mutable dans processor → immutable + Versioning ?

**7. Architecture hexagonale (bonus 3 min)**
• Où vois-tu le Domain pur (EnergyAggregate) vs Infrastructure ?
• Ports & Adapters : comment restructurerais-tu ce code hexagonal ?

## Quelques points d'évaluation

* Le projectDailyConsos() sur 1M events → quelle 1ère optim ? (batch upsert)
* Projections synchrone → pourquoi pas Kafka Streams séparé ?

## Réponses attendues

1. Première impression globale (2 min)
   Ce qui choque immédiatement :

Mélange des responsabilités dans le projet (projectDailyConsos fait des requêtes synchrones et des sauvegardes lourdes).

Usage naïf d’une liste en mémoire dans EventStore (implémentation mock) incompatible avec 1M events/h réels.

Implémentation réelle envisagée pour EventStore :

Kafka comme Event Store distribué à haute disponibilité, stockage durable des events via topics partitions.

S3 ou un stockage objet pour archivage longue durée et replay à froid.

Usage de timeuuid pour assurer ordonnancement.

2. Event Sourcing / CQRS (5 min)
   Découpage EventStore vs DailyConso projections :

Correct dans l’esprit, séparation écriture (append event immuable) et lecture (projections) claire.

Peut être amélioré en externalisant projections dans un process asynchrone (Kafka Streams).

Frontière Write Model vs Read Model :

Write Model = Event source unique vérité (append only), stocke events bruts.

Read Model = table projetée optimisée pour requêtes (ex: DailyConso en Cassandra).

EventStore append() : Kafka, S3 ou les deux ?

Kafka pour ingestion rapide, durabilité et résilience immédiates.

S3 pour archivage long terme, compliance, replay batch hors ligne.

3. Performance / Scalabilité (8 min)
   1M events/heure : projectDailyConsos() tient-il la route ?

Non, groupBy + N+1 queries → overhead massif, pas scalable.

Sauvegardes répétées synchrones → goulet d’étranglement.

Refactorisation prioritaire :

Refonte en batch upsert / merge sur Read Model ou Kafka Streams avec State Store.

Passage à architecture event-driven, asynchrone, découplée.

Projections synchrones → asynchrones ? Kafka Streams ou Flink ?

Oui, projections asynchrones déchargent ingestion.

Kafka Streams idéal pour tight integration Kafka-Cassandra.

Flink pour calculs complexes batch/stream ML.

4. Idempotence / Résilience (5 min)
   UUID idempotencyKey suffisant ?

Non, fragile, gestion manuelle lourde.

Kafka exactly-once (enable.idempotence, isolation.level=read_committed) bien meilleure.

Position DLQ, retry, circuit breaker ?

DLQ côté consommateur Kafka pour isoler données erronées.

Retry policy côté service avec délai exponentiel.

Circuit breaker sur appels DB/ autres services.

Garantir exactly-once sur toute la chaîne :

Kafka transactions + idempotent producers.

Atomicité dans projections (batch update).

Unicité clé logique dans read model.

5. Observabilité / Production (5 min)
   Métriques DORA clé :

Lead time for changes (temps dév → prod).

Mean time to recover (MTTR) incidents.

Deployment frequency.

Change failure rate.

Tracing distribué :

Corrélation trace MQTT event → Kafka message → projection update → alerte.

Utiliser OpenTelemetry ou Jaeger.

Logs :

Structurés, avec contextes (podId, eventId).

Niveaux adaptés : ERROR, WARN, INFO, DEBUG.

Corrélations entre services et composants.

6. Testabilité / Architecture (5 min)
   Tester end-to-end :

Architecturer tests utilisant des topics Kafka en test (Embedded Kafka).

Simuler ingestion, vérifier projections en Cassandra, vérifier alertes.

Tests contractuels, intégration continue complète.

DailyConso mutable → immutable + versioning :

Immutable projections = state store Kafka Streams.

Versioning pour audit, diff, rollbacks.

7. Architecture hexagonale (bonus 3 min)
   Domain pur vs Infrastructure :

Domain = EnergyAggregate, règles métier, events, invariants.

Infrastructure = Kafka, Cassandra, REST APIs, JDBC, messaging clients.

Ports & Adapters :

Ports = interfaces EventStore, ProjectionRepository, Publisher.

Adapters = implémentations KafkaEventStore, CassandraProjectionAdapter.

EnergyEventProcessor = Application Service orchestrant Domain + Ports.


