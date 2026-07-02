# Plan de migration hexagonale de l'objet Indicateur

## Contexte / état des lieux

- Une migration *partielle* existe déjà : `IndicatorsResources` est bien dans
  `modules/operations/indicators/webservice/`, **mais** elle dépend encore
  directement des services legacy (`OperationsService`, `IndicatorsUtils`) — ce
  n'est pas hexagonal.
- Le modèle cible (référence) est `modules/concepts/collections`, qui suit la
  structure : `domain/model`, `domain/exceptions`, `domain/port/clientside`
  (`@ClientSidePort`, interface finissant par `Service`), `domain/port/serverside`
  (`@ServerSidePort`), `domain/DomainXxxService`, `infrastructure/graphdb`
  (`@ServerSideAdaptor @Repository`), `webservice`, et une `XxxConfiguration`.
- **Point clé ArchUnit** : `HexagonaleArchTest` ne scanne aujourd'hui que
  `concepts.collections`, `users`, `clientconfig`. Le module `operations.indicators`
  n'est donc pas encore contrôlé — pour « respecter et valider » les règles, il
  faudra l'ajouter au scan et s'y conformer.
- L'`Indicator` legacy est riche (CRUD complet + XML + SIMS + publication + liens
  `replaces`/`seeAlso`/`wasGeneratedBy` + création d'ID via SPARQL). Les données
  e2e existent : `all-operations-and-indicators.trig` contient des indicateurs
  comme `p1651`, `p1623`, `p1636`.
- La sécurité est désactivée dans le profil de test (cf. `FamilyResourcesE2ETest`
  qui n'envoie pas d'auth).

## Décisions de périmètre

- **Périmètre** : maximal — CRUD + SIMS + search (tous les endpoints).
- **Infrastructure** : réimplémentation propre du SPARQL dans la nouvelle infra
  (sans dépendre de `IndicatorsUtils`).

## Cible : structure des packages (miroir de `concepts.collections`)

```
modules/operations/indicators/
├── IndicatorsConfiguration.java                    @Configuration (wiring des beans domaine)
├── domain/
│   ├── DomainIndicatorsService.java                implements IndicatorsService
│   ├── model/
│   │   ├── Indicator.java                           agrégat riche (immuable)
│   │   ├── IndicatorId.java                         value object (validation)
│   │   ├── PartialIndicator.java                    projection liste
│   │   ├── OperationLink.java / Organization…       VOs des liens
│   │   ├── commands/CreateIndicatorCommand.java
│   │   ├── commands/UpdateIndicatorCommand.java
│   │   └── package-info.java
│   ├── exceptions/                                  *Exception (règle NamingArchTest)
│   │   ├── IndicatorsFetchException / IndicatorsSaveException
│   │   ├── InvalidIndicatorIdException
│   │   ├── IndicatorNotFoundException
│   │   └── InvalidIndicatorCommandException (prefLabel unicité, wasGeneratedBy vide…)
│   └── port/
│       ├── clientside/IndicatorsService.java        @ClientSidePort, interface, finit par "Service"
│       └── serverside/IndicatorsRepository.java     @ServerSidePort, interface
├── infrastructure/graphdb/
│   ├── GraphDBIndicatorsRepository.java             @ServerSideAdaptor @Repository, SPARQL réimplémenté
│   ├── GraphDBIndicator.java / GraphDBPartialIndicator.java   mapping DTOs
│   └── (requêtes SPARQL dédiées au module)
└── webservice/
    ├── IndicatorsResources.java                     @RestController, dépend UNIQUEMENT de IndicatorsService
    ├── PartialOperationIndicatorResponse.java        (existe déjà)
    ├── IndicatorResponse.java
    ├── CreateIndicatorRequest.java / UpdateIndicatorRequest.java
    └── …
```

## Mapping endpoints → couche domaine

| Endpoint actuel | Méthode `IndicatorsService` |
|---|---|
| `GET /operations/indicators` | `getAllIndicators()` → `List<PartialIndicator>` |
| `GET /operations/indicator/{id}` (JSON + XML) | `getIndicator(IndicatorId)` → `Optional<Indicator>` |
| `POST /operations/indicator` | `createIndicator(CreateIndicatorCommand)` → `IndicatorId` |
| `PUT /operations/indicator/{id}` | `updateIndicator(UpdateIndicatorCommand)` |
| `PUT /operations/indicator/{id}/validate` | `validateIndicator(IndicatorId)` |
| `GET /operations/indicators/withSims` | `getIndicatorsWithSims()` |
| `GET /operations/indicators/advanced-search` | `getIndicatorsForSearch()` |

## Phases (chacune vérifiée avant de passer à la suivante)

### Phase 0 — Baseline e2e (AVANT toute migration)

- Écrire `IndicatorsResourcesE2ETest extends BaseE2ETest` couvrant les 7 endpoints
  contre les données `.trig` (ex. `p1651`, `p1623`) : liste + tri + self-links,
  getById JSON, getById XML (`Accept: application/xml`), withSims, advanced-search,
  et un cycle create→getById→update→validate.
- Lancer `mvn test -Dgroups=integration` (ciblé) → **confirmer que tout passe avec
  le code legacy actuel**. Ces tests deviennent le contrat de non-régression.

### Phase 1 — Domaine

Modèle, exceptions, ports clientside/serverside, `DomainIndicatorsService`.
Tests unitaires domaine.

### Phase 2 — Infrastructure

`GraphDBIndicatorsRepository` avec SPARQL réimplémenté proprement (en s'appuyant sur
`RepositoryGestion`/`RdfUtils` mais sans `IndicatorsUtils`), mappings DTO.
Attention aux points sensibles : conversion XHTML↔Markdown, feature-flag
`indicators-rich-text-new-structure`, génération d'ID (`lastID`), unicité prefLabel,
publication, sortie XML.

### Phase 3 — Webservice

Réécrire `IndicatorsResources` pour ne dépendre que de `IndicatorsService` ; DTOs
request/response ; gestion d'erreurs via `ResponseStatusException` (pas de
`@ControllerAdvice`, cf. `ForbiddenApiArchTest`).

### Phase 4 — Wiring

`IndicatorsConfiguration`. Débrancher la dépendance legacy.

### Phase 5 — ArchUnit

Ajouter `"fr.insee.rmes.modules.operations.indicators"` au `@AnalyzeClasses` de
`HexagonaleArchTest` + l'ajouter à `TestNamingArchTest`. Faire passer toutes les
règles (domaine ne dépend que du domaine, ports = interfaces, adaptor implémente
serverside port, etc.).

### Phase 6 — Vérification finale & itération

Relancer e2e (Phase 0) + tests unitaires + ArchUnit. **Itérer jusqu'au vert.**
Nettoyer le legacy indicateur devenu mort si possible (sinon le documenter).

## Multiagent

Une fois le domaine + ports figés (Phase 1), possibilité de paralléliser via
sous-agents : un pour l'infra SPARQL (Phase 2), un pour le webservice+DTOs (Phase 3),
un pour compléter les e2e. À lancer seulement à ce moment-là.

## Points de vigilance / risques

- **Sortie XML** (`getIndicatorByID` avec `Accept: application/xml`) et
  **withSims/advanced-search** renvoient aujourd'hui du JSON/XML brut construit par
  le legacy → réimplémenter proprement tout en gardant un format identique est la
  partie la plus risquée. Les e2e de Phase 0 sont le garde-fou.
- **SIMS** : `updateDocumentationTitle` couple l'indicateur à la documentation → à
  modéliser via un port ou à conserver comme dépendance explicite.
- **Génération d'ID** côté serveur (pas de `RandomIdGenerator` comme collections) →
  responsabilité du repository/port.
