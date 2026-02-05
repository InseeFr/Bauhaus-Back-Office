# Bauhaus Colectica Proxy

Proxy Netlify pour l'API Colectica. Ce projet sert de couche intermédiaire entre votre application Bauhaus et l'API Colectica, en gérant l'authentification de manière sécurisée.

## Fonctionnalités

- Gestion automatique des credentials Colectica (username/password)
- Proxy transparent pour tous les endpoints de l'API Colectica
- Support CORS pour les applications frontend
- Déploiement facile sur Netlify

## Endpoints disponibles

### 1. Authentication Token (`/token/createtoken`)

**Spécificité**: Ce endpoint surcharge le body de la requête pour injecter automatiquement les credentials depuis les variables d'environnement.

- **Méthode**: POST
- **URL**: `/.netlify/functions/token/createtoken`
- **Body**: Aucun (les credentials sont injectés automatiquement)
- **Réponse**: Token d'authentification

```javascript
// Exemple d'utilisation
const response = await fetch('https://your-proxy.netlify.app/.netlify/functions/token/createtoken', {
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
- **URL**: `/.netlify/functions/api/v1/_query`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
```json
{
  "itemTypes": ["a51e85bb-6259-4488-8df2-f08cb43485f8"]
}
```

```javascript
// Exemple d'utilisation
const response = await fetch('https://your-proxy.netlify.app/.netlify/functions/api/v1/_query', {
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
- **URL**: `/.netlify/functions/api/v1/ddiset/{agencyId}/{identifier}`
- **Headers**: `Authorization: Bearer {token}`

```javascript
// Exemple d'utilisation
const response = await fetch(
  'https://your-proxy.netlify.app/.netlify/functions/api/v1/ddiset/fr.insee/12345-67890',
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
- **URL**: `/.netlify/functions/api/v1/item`
- **Headers**: `Authorization: Bearer {token}`
- **Body**: Objet contenant les items à créer/mettre à jour

```javascript
// Exemple d'utilisation
const response = await fetch('https://your-proxy.netlify.app/.netlify/functions/api/v1/item', {
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

### 1. Installation des dépendances

```bash
cd netlify-proxy
npm install
```

### 2. Configuration des variables d'environnement

Copiez le fichier `.env.example` vers `.env` et remplissez les valeurs:

```bash
cp .env.example .env
```

Éditez le fichier `.env`:

```env
COLECTICA_BASE_URL=https://your-colectica-server.com
COLECTICA_BASE_API_URL=https://your-colectica-server.com/api/v1/
COLECTICA_USERNAME=your-username
COLECTICA_PASSWORD=your-password
```

### 3. Développement local

```bash
npm run dev
```

Le proxy sera disponible sur `http://localhost:8888`

## Déploiement sur Netlify

### Option 1: Via l'interface Netlify

1. Créez un nouveau site sur Netlify
2. Connectez votre repository Git
3. Configurez les variables d'environnement dans les paramètres du site:
   - `COLECTICA_BASE_URL`
   - `COLECTICA_BASE_API_URL`
   - `COLECTICA_USERNAME`
   - `COLECTICA_PASSWORD`
4. Déployez

### Option 2: Via la CLI Netlify

```bash
# Installation de la CLI (si nécessaire)
npm install -g netlify-cli

# Login
netlify login

# Initialisation du site
netlify init

# Configuration des variables d'environnement
netlify env:set COLECTICA_BASE_URL "https://your-colectica-server.com"
netlify env:set COLECTICA_BASE_API_URL "https://your-colectica-server.com/api/v1/"
netlify env:set COLECTICA_USERNAME "your-username"
netlify env:set COLECTICA_PASSWORD "your-password"

# Déploiement en production
npm run deploy:prod
```

## Utilisation dans votre application Java

Pour utiliser ce proxy dans votre application Bauhaus, modifiez la configuration pour pointer vers l'URL Netlify:

```properties
# application.properties
colectica.base-server-url=https://your-proxy.netlify.app/.netlify/functions
colectica.base-api-url=https://your-proxy.netlify.app/.netlify/functions/api/v1/

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
│ Netlify Proxy           │
│                         │
│ Functions:              │
│ - /token/createtoken    │ ← Inject credentials
│ - /api/v1/_query        │ ← Simple proxy
│ - /api/v1/ddiset        │ ← Simple proxy
│ - /api/v1/item          │ ← Simple proxy
└────────┬────────────────┘
         │
         │ HTTP Requests (with credentials)
         ▼
┌─────────────────┐
│ Colectica API   │
└─────────────────┘
```

## Sécurité

- Les credentials Colectica sont stockés uniquement dans les variables d'environnement Netlify
- Aucun credential n'est exposé côté client
- Support CORS configuré pour accepter les requêtes depuis votre frontend
- Les tokens d'authentification sont transmis via les headers HTTP

## Logs

Les logs sont disponibles dans l'interface Netlify sous "Functions" → "Function logs". Vous y trouverez:

- Les requêtes proxifiées
- Les erreurs d'authentification
- Les détails de chaque appel API

## Troubleshooting

### Erreur d'authentification

Si vous recevez une erreur 401:

1. Vérifiez que les variables d'environnement sont correctement configurées
2. Vérifiez que le username et password sont corrects
3. Consultez les logs Netlify pour plus de détails

### Erreur CORS

Si vous rencontrez des erreurs CORS:

1. Vérifiez que votre frontend envoie les bonnes headers
2. Vérifiez que l'URL du proxy est correcte
3. Assurez-vous que le proxy est bien déployé

### Timeout

Si les requêtes timeout:

1. Les fonctions Netlify ont un timeout de 10 secondes par défaut (gratuit) ou 26 secondes (payant)
2. Si vos requêtes prennent plus de temps, envisagez d'optimiser ou d'utiliser un plan payant

## License

MIT
