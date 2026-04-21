# Colectica Code List Deny List

## Vue d'ensemble

Le système de deny list permet de filtrer automatiquement certaines code lists des résultats retournés par l'API Colectica. Ceci est utile pour exclure :

- Des code lists dépréciées ou obsolètes
- Des code lists de test ou temporaires
- Des code lists qui ne doivent pas être visibles dans l'application

## Architecture

### Composants principaux

1. **ColecticaConfiguration** - Configuration Spring Boot avec la deny list
2. **ColecticaConfigurationValidator** - Validation au démarrage de l'application
3. **DenyListFilter** - Composant réutilisable pour appliquer le filtrage
4. **DDIRepositoryImpl** - Implémentation qui utilise le filtrage avec cache
5. **ColecticaAdminController** - Endpoints d'administration pour monitoring

### Performance

Le système utilise un cache HashSet pour des recherches en **O(1)**, optimisé pour de grandes deny lists.

## Configuration

### Fichier de configuration

Ajoutez les entrées dans `bauhaus-core.properties` :

```properties
# Code List Deny List - Liste des codes lists à exclure
# Exemple: Liste des statuts professionnels
fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].agency-id = fr.insee
fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].id = 2a22ba00-a977-4a61-a582-99025c6b0582

# Autre exemple
fr.insee.rmes.bauhaus.colectica.code-list-deny-list[1].agency-id = fr.insee
fr.insee.rmes.bauhaus.colectica.code-list-deny-list[1].id = autre-id-a-exclure
```

### Validation

La configuration est automatiquement validée au démarrage :
- Les champs `agencyId` et `id` ne peuvent pas être null ou vides
- L'application ne démarre pas si la configuration est invalide
- Les erreurs de validation sont clairement affichées dans les logs

### Désactiver le filtrage

Pour désactiver le filtrage, commentez ou supprimez toutes les entrées de la deny list :

```properties
# fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].agency-id = fr.insee
# fr.insee.rmes.bauhaus.colectica.code-list-deny-list[0].id = 2a22ba00-a977-4a61-a582-99025c6b0582
```

## Utilisation

### API principale

Le filtrage est appliqué automatiquement lors de l'appel à :

```java
List<PartialCodesList> codeLists = ddiRepository.getCodesLists();
// Les code lists dans la deny list sont automatiquement exclues
```

### Utilisation du DenyListFilter

Le composant `DenyListFilter` peut être réutilisé pour d'autres types d'items :

```java
@Autowired
private DenyListFilter denyListFilter;

public List<MyItem> getFilteredItems(List<MyItem> items) {
    return denyListFilter.filterItems(
        items,
        MyItem::getAgencyId,  // Extracteur d'agencyId
        MyItem::getId         // Extracteur d'id
    );
}
```

### Vérification manuelle

Pour vérifier si un item est dans la deny list :

```java
boolean isDenied = denyListFilter.isInDenyList("fr.insee", "some-id");
```

## Endpoints d'administration

### GET /api/admin/colectica/deny-list

Retourne la configuration actuelle de la deny list.

**Réponse:**
```json
[
  {
    "agencyId": "fr.insee",
    "id": "2a22ba00-a977-4a61-a582-99025c6b0582"
  }
]
```

### GET /api/admin/colectica/deny-list/check

Vérifie si un item spécifique est dans la deny list.

**Paramètres:**
- `agencyId` - Agency ID à vérifier
- `id` - ID de la code list à vérifier

**Exemple:**
```
GET /api/admin/colectica/deny-list/check?agencyId=fr.insee&id=2a22ba00-a977-4a61-a582-99025c6b0582
```

**Réponse:**
```json
{
  "agencyId": "fr.insee",
  "id": "2a22ba00-a977-4a61-a582-99025c6b0582",
  "isInDenyList": true,
  "willBeFiltered": true
}
```

### GET /api/admin/colectica/deny-list/stats

Retourne des statistiques sur le cache de la deny list.

**Réponse:**
```json
{
  "configuredEntries": 2,
  "cacheSize": 2,
  "cacheInitialized": true,
  "denyListActive": true
}
```

### GET /api/admin/colectica/deny-list/health

Health check pour la deny list.

**Réponse:**
```json
{
  "status": "UP",
  "denyListConfigured": true,
  "filterComponentAvailable": true
}
```

## Logging

### Niveaux de log

- **INFO** - Initialisation du cache, nombre d'items filtrés
- **DEBUG** - Détails de chaque item filtré
- **WARN** - Problèmes de configuration (non bloquants)
- **ERROR** - Erreurs de validation (bloquantes au démarrage)

### Exemples de logs

```
INFO  - Initialized code list deny list cache with 2 entries
INFO  - Received 15 code lists from Colectica API
INFO  - Filtered 2 code list(s) from 15 total using deny list (returned 13 code lists)
DEBUG - Filtering out code list: agencyId=fr.insee, id=2a22ba00-a977-4a61-a582-99025c6b0582
```

## Tests

Des tests unitaires complets sont disponibles dans `DDIRepositoryImplTest` :

- `shouldFilterCodeListsInDenyList()` - Vérification du filtrage
- `shouldNotFilterWhenDenyListIsEmpty()` - Comportement avec liste vide
- `shouldNotFilterWhenDenyListIsNull()` - Comportement avec liste null
- `shouldFilterMultipleCodeListsInDenyList()` - Filtrage multiple

## Sécurité

⚠️ **Important:** Les endpoints d'administration (`/api/admin/colectica/*`) doivent être sécurisés en production pour empêcher l'accès non autorisé aux informations de configuration.

Ajoutez la sécurité appropriée dans votre configuration Spring Security.

## Performance

- **Lookup:** O(1) grâce au cache HashSet
- **Initialisation:** O(n) lors du premier accès (lazy loading)
- **Mémoire:** Proportionnelle au nombre d'entrées dans la deny list

Pour une deny list de 1000 entrées :
- Temps d'initialisation : ~1-2ms
- Temps de lookup : < 1µs
- Mémoire utilisée : ~50KB

## Troubleshooting

### L'application ne démarre pas

Vérifiez que toutes les entrées de la deny list ont des valeurs valides :
```
ERROR - Code list deny list entry at index 0 has null or empty agencyId
```

Solution : Vérifiez votre fichier `bauhaus-core.properties`

### Les code lists ne sont pas filtrées

1. Vérifiez que la deny list est configurée :
   ```
   GET /api/admin/colectica/deny-list/stats
   ```

2. Vérifiez que les valeurs correspondent exactement (case-sensitive) :
   ```
   GET /api/admin/colectica/deny-list/check?agencyId=fr.insee&id=...
   ```

3. Vérifiez les logs pour voir si le cache est initialisé :
   ```
   INFO - Initialized code list deny list cache with N entries
   ```

### Performance dégradée

Si vous avez une très grande deny list (> 10000 entrées), considérez :
- Revoir la stratégie de filtrage
- Utiliser un filtrage côté base de données
- Implémenter un cache distribué (Redis)

## Évolutions futures possibles

- Support des patterns regex pour les IDs
- Filtrage par d'autres critères (date, label, etc.)
- Interface d'administration web pour gérer la deny list
- Gestion de la deny list via base de données
- Métriques Prometheus pour monitoring
- Support de l'allowList (liste blanche) en plus de la denyList
