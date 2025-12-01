# Bauhaus Colectica Proxy - Firebase Functions

Proxy Firebase pour l'API Colectica. Ce projet sert de couche intermédiaire entre votre application Bauhaus et l'API Colectica, en gérant l'authentification de manière sécurisée.

## Fonctionnalités

- Gestion automatique des credentials Colectica (username/password)
- Proxy transparent pour tous les endpoints de l'API Colectica
- Support CORS pour les applications frontend
- Déploiement facile sur Firebase

## Endpoints disponibles

### 1. Authentication Token (`/token/createtoken`)

**Spécificité**: Ce endpoint surcharge le body de la requête pour injecter automatiquement les credentials depuis les variables d'environnement.

- **Méthode**: POST
- **URL**: `https://<project-id>.web.app/token/createtoken`
- **Body**: Aucun (les credentials sont injectés automatiquement)
- **Réponse**: Token d'authentification

```javascript
// Exemple d'utilisation
const response = await fetch('https://<project-id>.web.app/token/createtoken', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  }
});

const { accessToken } = await response.json();
```

### 2. Query Physical Instances (`/api/v1/_query`)

Liste les instances physiques disponibles.

- **Méthode**: POST
- **URL**: `https://<project-id>.web.app/api/v1/_query`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
```json
{
  "itemTypes": ["a51e85bb-6259-4488-8df2-f08cb43485f8"]
}
```

```javascript
// Exemple d'utilisation
const response = await fetch('https://<project-id>.web.app/api/v1/_query', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({
    itemTypes: ["a51e85bb-6259-4488-8df2-f08cb43485f8"]
  })
});

const instances = await response.json();
```

### 3. Get DDI Set (`/api/v1/ddiset/{agencyId}/{identifier}`)

Récupère un ensemble DDI complet (PhysicalInstance + DataRelationship).

- **Méthode**: GET
- **URL**: `https://<project-id>.web.app/api/v1/ddiset/{agencyId}/{identifier}`
- **Headers**: `Authorization: Bearer {token}`

```javascript
// Exemple d'utilisation
const response = await fetch(
  'https://<project-id>.web.app/api/v1/ddiset/fr.insee/12345-67890',
  {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  }
);

const ddiset = await response.text(); // XML response
```

### 4. Create/Update Item (`/api/v1/item`)

Crée ou met à jour des items dans Colectica.

- **Méthode**: POST
- **URL**: `https://<project-id>.web.app/api/v1/item`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: Objet contenant les items à créer/mettre à jour

```javascript
// Exemple d'utilisation
const response = await fetch('https://<project-id>.web.app/api/v1/item', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${accessToken}`
  },
  body: JSON.stringify({
    items: [
      // Your items here
    ]
  })
});
```

## Installation

### Prérequis

- Node.js 20 ou supérieur
- Un compte Firebase (https://console.firebase.google.com)
- Firebase CLI installé globalement: `npm install -g firebase-tools`

### 1. Créer un projet Firebase

1. Allez sur https://console.firebase.google.com
2. Créez un nouveau projet ou sélectionnez un projet existant
3. Activez Firebase Functions dans votre projet

### 2. Configuration du projet local

#### a. Modifier .firebaserc

Éditez le fichier `.firebaserc` et remplacez `your-project-id` par l'ID de votre projet Firebase:

```json
{
  "projects": {
    "default": "votre-project-id-firebase"
  }
}
```

#### b. Installation des dépendances

```bash
cd functions
npm install
cd ..
```

### 3. Configuration des variables d'environnement

Firebase Functions v2 utilise un système de variables d'environnement différent de Netlify.

#### Pour le développement local:

Créez un fichier `.env` dans le dossier `functions/`:

```bash
cd functions
cp .env.example .env
```

Éditez le fichier `.env`:

```env
COLECTICA_BASE_URL=https://your-colectica-server.com
COLECTICA_BASE_API_URL=https://your-colectica-server.com/api/v1/
COLECTICA_USERNAME=your-username
COLECTICA_PASSWORD=your-password
```

#### Pour la production (Firebase):

Vous devez définir les secrets Firebase pour chaque variable:

```bash
# Login to Firebase
firebase login

# Set secrets (vous serez invité à entrer la valeur pour chaque secret)
firebase functions:secrets:set COLECTICA_BASE_URL
firebase functions:secrets:set COLECTICA_BASE_API_URL
firebase functions:secrets:set COLECTICA_USERNAME
firebase functions:secrets:set COLECTICA_PASSWORD
```

Ou en une seule ligne pour chaque secret:

```bash
echo "https://your-colectica-server.com" | firebase functions:secrets:set COLECTICA_BASE_URL
echo "https://your-colectica-server.com/api/v1/" | firebase functions:secrets:set COLECTICA_BASE_API_URL
echo "your-username" | firebase functions:secrets:set COLECTICA_USERNAME
echo "your-password" | firebase functions:secrets:set COLECTICA_PASSWORD
```

### 4. Développement local

Pour tester localement avec l'émulateur Firebase:

```bash
firebase emulators:start --only functions
```

Les fonctions seront disponibles sur `http://localhost:5001/<project-id>/<region>/functionName`

Par exemple:
- `http://localhost:5001/<project-id>/us-central1/createtoken`
- `http://localhost:5001/<project-id>/us-central1/query`
- etc.

## Déploiement sur Firebase

### Déploiement de toutes les fonctions

```bash
firebase deploy --only functions
```

### Déploiement d'une fonction spécifique

```bash
firebase deploy --only functions:createtoken
firebase deploy --only functions:query
firebase deploy --only functions:item
firebase deploy --only functions:ddiset
```

### Après le déploiement

Firebase vous fournira les URLs des fonctions déployées:

```
✔  functions[createtoken(us-central1)]: Successful create operation.
Function URL (createtoken): https://us-central1-<project-id>.cloudfunctions.net/createtoken
```

Notez ces URLs pour configurer votre application Java.

## Utilisation dans votre application Java

Pour utiliser ce proxy dans votre application Bauhaus, modifiez la configuration pour pointer vers les URLs Firebase:

```properties
# application.properties
colectica.base-server-url=https://us-central1-<project-id>.cloudfunctions.net
colectica.base-api-url=https://us-central1-<project-id>.cloudfunctions.net

# Mappings des endpoints:
# /token/createtoken -> /createtoken
# /api/v1/_query -> /query
# /api/v1/item -> /item
# /api/v1/ddiset -> /ddiset

# Vous n'avez plus besoin de ces variables car elles sont gérées par le proxy
# colectica.username=
# colectica.password=
```

## Architecture

```
┌─────────────────┐
│  Bauhaus API    │
└────────┬────────┘
         │
         │ HTTP Requests
         ▼
┌─────────────────────────┐
│ Firebase Functions      │
│                         │
│ Functions:              │
│ - /createtoken          │ ← Inject credentials
│ - /query                │ ← Simple proxy
│ - /ddiset               │ ← Simple proxy
│ - /item                 │ ← Simple proxy
└────────┬────────────────┘
         │
         │ HTTP Requests (with credentials)
         ▼
┌─────────────────┐
│ Colectica API   │
└─────────────────┘
```

## Sécurité

- Les credentials Colectica sont stockés de manière sécurisée dans Firebase Secrets
- Aucun credential n'est exposé côté client
- Support CORS configuré pour accepter les requêtes depuis votre frontend
- Les tokens d'authentification sont transmis via les headers HTTP
- Les secrets Firebase sont chiffrés au repos et en transit

## Logs et monitoring

### Consulter les logs

```bash
# Logs en temps réel
firebase functions:log

# Logs d'une fonction spécifique
firebase functions:log --only createtoken

# Logs sur la console Firebase
# Allez sur https://console.firebase.google.com
# Sélectionnez votre projet > Functions > Logs
```

Vous y trouverez:
- Les requêtes proxifiées
- Les erreurs d'authentification
- Les détails de chaque appel API

## Coûts

Firebase Functions a un plan gratuit (Spark) qui inclut:
- 2 millions d'invocations par mois
- 400 000 Go-secondes de calcul
- 200 000 Go-secondes de mémoire
- 5 Go de transfert réseau sortant

Pour des besoins plus importants, passez au plan Blaze (pay-as-you-go).

## Troubleshooting

### Erreur d'authentification

Si vous recevez une erreur 401:

1. Vérifiez que les secrets Firebase sont correctement configurés:
   ```bash
   firebase functions:secrets:access COLECTICA_USERNAME
   ```
2. Vérifiez que le username et password sont corrects
3. Consultez les logs Firebase pour plus de détails

### Erreur CORS

Si vous rencontrez des erreurs CORS:

1. Vérifiez que votre frontend envoie les bonnes headers
2. Vérifiez que l'URL du proxy est correcte
3. Les headers CORS sont déjà configurés dans le code

### Erreur de déploiement

Si le déploiement échoue:

1. Vérifiez que vous êtes connecté: `firebase login`
2. Vérifiez que votre projet est correctement configuré dans `.firebaserc`
3. Vérifiez que les dépendances sont installées dans `functions/`
4. Consultez les logs d'erreur pour plus de détails

### Timeout

Les fonctions Firebase ont un timeout de 60 secondes par défaut (540 secondes max). Si vos requêtes prennent plus de temps, vous pouvez ajuster le timeout dans le code:

```javascript
export const myFunction = onRequest(
  { timeoutSeconds: 300 }, // 5 minutes
  async (req, res) => { ... }
);
```

## Commandes utiles

```bash
# Voir la liste des fonctions déployées
firebase functions:list

# Voir les logs en temps réel
firebase functions:log --only <function-name>

# Supprimer une fonction
firebase functions:delete <function-name>

# Voir les secrets configurés
firebase functions:secrets:access <SECRET_NAME>

# Mettre à jour un secret
firebase functions:secrets:set <SECRET_NAME>

# Détruire un secret
firebase functions:secrets:destroy <SECRET_NAME>
```

## Différences avec Netlify

| Aspect | Netlify | Firebase |
|--------|---------|----------|
| Structure | Fonctions séparées dans des fichiers | Toutes les fonctions dans index.js |
| Variables d'env | Variables d'environnement simples | Secrets Firebase |
| Déploiement | `netlify deploy` | `firebase deploy` |
| URLs | `/.netlify/functions/path` | `/functionName` |
| Émulateur local | `netlify dev` | `firebase emulators:start` |
| Logs | Interface Netlify | `firebase functions:log` ou console |

## Migrations depuis Netlify

Si vous migrez depuis Netlify, vous devrez:

1. Mettre à jour les URLs dans votre application Java (voir ci-dessus)
2. Reconfigurer les variables d'environnement en tant que secrets Firebase
3. Adapter les chemins d'API (les paths ont changé pour plus de simplicité)

## License

MIT