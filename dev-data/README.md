# Jeu de données local — filtrage STAMP des PhysicalInstances

Reproduit en local le parcours d'un `Gestionnaire_variables_RMESGNCS`, dont la lecture des
PhysicalInstances (`ddi_physicalinstance.read: STAMP`) est limitée aux fichiers des séries créées
par son timbre.

## Contenu

| Série | `dc:creator` (gestion) | Ce que le cas vérifie | Attendu pour `DG75-T001` |
|---|---|---|---|
| `s9991` | IRI `…/organisations/insee/HIE9990001` | format courant | visible |
| `s9992` | littéral `"HIE9990001"` | format historique | visible |
| `s9993` | IRI `…/HIE9990002` (équipe T002) | autre équipe | invisible |
| `s9994` | aucun, famille `s9995` créée par T001 | créateur hérité de la famille | visible |

Organisations (graphe `organisations/insee`, base de gestion uniquement) :

| IRI | `adms:identifier` | `dcterms:identifier` |
|---|---|---|
| `…/HIE9990001` | `HIE9990001` | `DG75-T001` |
| `…/HIE9990002` | `HIE9990002` | `DG75-T002` |

- `ddi-stamp-gestion.trig` : séries, opérations, familles et organisations, IRI `http://bauhaus/…`.
- `ddi-stamp-publication.trig` : mêmes séries et opérations en IRI `http://id.insee.fr/…`. L'init
  Colectica lit la base de publication : ce sont ces IRI qui deviennent les `seriesIris` des groupes.

## Charger les données

```bash
for r in bauhaus:ddi-stamp-gestion.trig publication:ddi-stamp-publication.trig; do
  curl -X POST -H 'Content-Type: application/x-trig' \
    --data-binary @dev-data/${r#*:} http://localhost:7200/repositories/${r%%:*}/statements
done
```

Le chargement est idempotent (mêmes triplets).

## Créer les objets Colectica et se connecter en gestionnaire

```bash
java -jar module-bauhaus-bo/target/module-bauhaus-bo-*.jar \
  --fr.insee.rmes.bauhaus.colectica.init=true \
  --fr.insee.rmes.bauhaus.dev-user.roles=Gestionnaire_variables_RMESGNCS \
  --fr.insee.rmes.bauhaus.dev-user.stamps=DG75-T001 \
  --fr.insee.rmes.bauhaus.dev-user.source=ssm
```

- `colectica.init=true` crée, pour chaque série, des groupes et pour chaque opération des études
  et une PhysicalInstance. Les identifiants sont déterministes : une relance réécrit les mêmes objets.
- Les propriétés `dev-user.*` remplacent l'utilisateur fictif (par défaut administrateur, timbre
  `DG75-F302`, source `insee`). Elles peuvent aussi être décommentées dans
  `bauhaus-local-dev.properties`.
- La source `ssm` est indispensable pour tester le filtrage : un agent de source `insee` reçoit la
  lecture `ALL` sur tous les modules (`addInseeReadPrivileges`), liste des PI et des groupes comprise.
- Si la base locale ne contient pas l'organisation de `fr.insee.rmes.bauhaus.defaultContributor`,
  le démarrage échoue : passer une organisation existante, par exemple
  `--fr.insee.rmes.bauhaus.defaultContributor=http://bauhaus/organisations/insee/HIE2004993`.

## Changer d'utilisateur sans redémarrer

Hors PROD, chaque requête peut remplacer les champs de l'utilisateur fictif par des en-têtes
(valeurs séparées par des virgules, un en-tête absent garde la valeur configurée) :

```bash
curl -H 'X-Dev-User-Roles: Gestionnaire_variables_RMESGNCS' \
     -H 'X-Dev-User-Stamps: DG75-T001' \
     -H 'X-Dev-User-Source: ssm' \
     http://localhost:8080/api/ddi/physical-instance
```

Côté front, l'onglet **Utilisateur** des TanStack DevTools (`enableDevTools`) envoie ces en-têtes
à chaque appel. En PROD, le filtre n'existe pas : ces en-têtes n'authentifient rien.

## Le timbre de l'utilisateur

Le filtrage compare le timbre de l'utilisateur aux créateurs **tels qu'ils sont stockés** dans la
base de gestion : `DG75-T001`, `HIE9990001` et l'IRI de l'organisation sont trois écritures
différentes d'une même équipe. Changer `dev-user.stamps` permet de vérifier lesquelles sont
reconnues.
