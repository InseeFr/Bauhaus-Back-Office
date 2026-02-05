/**
 * Firebase Functions mock for Colectica API
 * Implementation with Firestore persistence
 */
import { onRequest } from 'firebase-functions/v2/https';
import { XMLParser } from 'fast-xml-parser';
import { initializeApp } from 'firebase-admin/app';
import { getFirestore } from 'firebase-admin/firestore';

// Initialize Firebase Admin
initializeApp();
const db = getFirestore();

// Collection name for items
const ITEMS_COLLECTION = 'items';

/**
 * Default data to populate the store on initialization
 */
const defaultItems = [
  // StudyUnits
  {
    ItemType: "752a535b-b548-4fbe-97e4-f26a02d9e413",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "89f5e04d-da22-485f-9c08-5fbe452b6c90",
    Item: ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <StudyUnit isUniversallyUnique="true" versionDate="2025-01-09T09:00:00.000000Z" xmlns="ddi:studyunit:3_3"> <r:URN>urn:ddi:fr.insee:89f5e04d-da22-485f-9c08-5fbe452b6c90:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>89f5e04d-da22-485f-9c08-5fbe452b6c90</r:ID> <r:Version>1</r:Version> <r:Citation> <r:Title> <r:String xml:lang="fr-FR">BPE 2021</r:String> </r:Title> </r:Citation> </StudyUnit> </Fragment>`,
    VersionDate: "2025-01-09T09:00:00.000000Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  {
    ItemType: "752a535b-b548-4fbe-97e4-f26a02d9e413",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "820a7c14-0ac4-42bc-a8c1-d39f60e304ee",
    Item: ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <StudyUnit isUniversallyUnique="true" versionDate="2025-01-09T09:00:00.000000Z" xmlns="ddi:studyunit:3_3"> <r:URN>urn:ddi:fr.insee:820a7c14-0ac4-42bc-a8c1-d39f60e304ee:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>820a7c14-0ac4-42bc-a8c1-d39f60e304ee</r:ID> <r:Version>1</r:Version> <r:Citation> <r:Title> <r:String xml:lang="fr-FR">BPE 2022</r:String> </r:Title> </r:Citation> </StudyUnit> </Fragment>`,
    VersionDate: "2025-01-09T09:00:00.000000Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  {
    ItemType: "752a535b-b548-4fbe-97e4-f26a02d9e413",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "948edb5b-96a5-4823-b45d-3019fcc40227",
    Item: ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <StudyUnit isUniversallyUnique="true" versionDate="2025-01-09T09:00:00.000000Z" xmlns="ddi:studyunit:3_3"> <r:URN>urn:ddi:fr.insee:948edb5b-96a5-4823-b45d-3019fcc40227:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>948edb5b-96a5-4823-b45d-3019fcc40227</r:ID> <r:Version>1</r:Version> <r:Citation> <r:Title> <r:String xml:lang="fr-FR">BPE 2023</r:String> </r:Title> </r:Citation> </StudyUnit> </Fragment>`,
    VersionDate: "2025-01-09T09:00:00.000000Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  {
    ItemType: "752a535b-b548-4fbe-97e4-f26a02d9e413",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "5bd79348-ea1f-4f3c-8eff-63452147a050",
    Item: ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <StudyUnit isUniversallyUnique="true" versionDate="2025-01-09T09:00:00.000000Z" xmlns="ddi:studyunit:3_3"> <r:URN>urn:ddi:fr.insee:5bd79348-ea1f-4f3c-8eff-63452147a050:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>5bd79348-ea1f-4f3c-8eff-63452147a050</r:ID> <r:Version>1</r:Version> <r:Citation> <r:Title> <r:String xml:lang="fr-FR">BPE 2024</r:String> </r:Title> </r:Citation> </StudyUnit> </Fragment>`,
    VersionDate: "2025-01-09T09:00:00.000000Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  // Groups
  {
    ItemType: "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "10a689ce-7006-429b-8e84-036b7787b422",
    Item: ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <Group isUniversallyUnique="true" versionDate="2025-01-09T09:00:00.000000Z" xmlns="ddi:group:3_3"> <r:URN>urn:ddi:fr.insee:10a689ce-7006-429b-8e84-036b7787b422:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>10a689ce-7006-429b-8e84-036b7787b422</r:ID> <r:Version>1</r:Version> <r:VersionResponsibility>abcde</r:VersionResponsibility> <r:Citation> <r:Title> <r:String xml:lang="fr-FR">Base permanente des équipements</r:String> </r:Title> </r:Citation> <r:StudyUnitReference> <r:Agency>fr.insee</r:Agency> <r:ID>89f5e04d-da22-485f-9c08-5fbe452b6c90</r:ID> <r:Version>1</r:Version> <r:TypeOfObject>StudyUnit</r:TypeOfObject> </r:StudyUnitReference> <r:StudyUnitReference> <r:Agency>fr.insee</r:Agency> <r:ID>820a7c14-0ac4-42bc-a8c1-d39f60e304ee</r:ID> <r:Version>1</r:Version> <r:TypeOfObject>StudyUnit</r:TypeOfObject> </r:StudyUnitReference> <r:StudyUnitReference> <r:Agency>fr.insee</r:Agency> <r:ID>948edb5b-96a5-4823-b45d-3019fcc40227</r:ID> <r:Version>1</r:Version> <r:TypeOfObject>StudyUnit</r:TypeOfObject> </r:StudyUnitReference> <r:StudyUnitReference> <r:Agency>fr.insee</r:Agency> <r:ID>5bd79348-ea1f-4f3c-8eff-63452147a050</r:ID> <r:Version>1</r:Version> <r:TypeOfObject>StudyUnit</r:TypeOfObject> </r:StudyUnitReference> </Group> </Fragment>`,
    VersionDate: "2025-01-09T09:00:00.000000Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  {
    ItemType: "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "20b789de-8007-530c-9f95-147c75e6cb34",
    Item: ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <Group isUniversallyUnique="true" versionDate="2025-01-09T10:00:00.000000Z" xmlns="ddi:group:3_3"> <r:URN>urn:ddi:fr.insee:20b789de-8007-530c-9f95-147c75e6cb34:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>20b789de-8007-530c-9f95-147c75e6cb34</r:ID> <r:Version>1</r:Version> <r:VersionResponsibility>abcde</r:VersionResponsibility> <r:Citation> <r:Title> <r:String xml:lang="fr-FR">Recensement de la population</r:String> </r:Title> </r:Citation> </Group> </Fragment>`,
    VersionDate: "2025-01-09T10:00:00.000000Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  {
    ItemType: "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "30c890ef-9008-631d-0a06-258d86f7dc45",
    Item: ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <Group isUniversallyUnique="true" versionDate="2025-01-09T11:00:00.000000Z" xmlns="ddi:group:3_3"> <r:URN>urn:ddi:fr.insee:30c890ef-9008-631d-0a06-258d86f7dc45:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>30c890ef-9008-631d-0a06-258d86f7dc45</r:ID> <r:Version>1</r:Version> <r:VersionResponsibility>abcde</r:VersionResponsibility> <r:Citation> <r:Title> <r:String xml:lang="fr-FR">Enquête emploi en continu</r:String> </r:Title> </r:Citation> </Group> </Fragment>`,
    VersionDate: "2025-01-09T11:00:00.000000Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  // CodeLists
  {
    ItemType: "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "2a22ba00-a977-4a61-a582-99025c6b0582",
    Item: " <Fragment xmlns=\"ddi:instance:3_3\" xmlns:r=\"ddi:reusable:3_3\"> <CodeList isUniversallyUnique=\"true\" versionDate=\"2023-07-04T09:19:29.3053289Z\" xmlns=\"ddi:logicalproduct:3_3\"> <r:URN>urn:ddi:fr.insee:2a22ba00-a977-4a61-a582-99025c6b0582:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>2a22ba00-a977-4a61-a582-99025c6b0582</r:ID> <r:Version>1</r:Version> <r:Label> <r:Content xml:lang=\"fr-FR\">Liste des statuts professionnels</r:Content> </r:Label> <Code isUniversallyUnique=\"true\"> <r:URN>urn:ddi:fr.insee:a4648d8f-a4cc-4f1a-be51-3ddc914597e5:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>a4648d8f-a4cc-4f1a-be51-3ddc914597e5</r:ID> <r:Version>1</r:Version> <r:CategoryReference> <r:Agency>fr.insee</r:Agency> <r:ID>d597f327-773f-4ae8-852f-ae04166827da</r:ID> <r:Version>1</r:Version> <r:TypeOfObject>Category</r:TypeOfObject> </r:CategoryReference> <r:Value>0</r:Value> </Code> , <Code isUniversallyUnique=\"true\"> <r:URN>urn:ddi:fr.insee:c65cafcf-d410-4f8c-a983-3278e72c4c70:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>c65cafcf-d410-4f8c-a983-3278e72c4c70</r:ID> <r:Version>1</r:Version> <r:CategoryReference> <r:Agency>fr.insee</r:Agency> <r:ID>d1ee8459-65ec-40e2-a7e3-f1809e1b1b5a</r:ID> <r:Version>1</r:Version> <r:TypeOfObject>Category</r:TypeOfObject> </r:CategoryReference> <r:Value>1</r:Value> </Code> </CodeList> </Fragment> ",
    VersionDate: "2023-07-04T09:19:29.3053289Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  {
    ItemType: "7e47c269-bcab-40f7-a778-af7bbc4e3d00",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "d597f327-773f-4ae8-852f-ae04166827da",
    Item: " <Fragment xmlns=\"ddi:instance:3_3\" xmlns:r=\"ddi:reusable:3_3\"> <Category isUniversallyUnique=\"true\" versionDate=\"2023-07-04T09:19:29.3073232Z\" xmlns=\"ddi:logicalproduct:3_3\"> <r:URN>urn:ddi:fr.insee:d597f327-773f-4ae8-852f-ae04166827da:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>d597f327-773f-4ae8-852f-ae04166827da</r:ID> <r:Version>1</r:Version> <r:Label> <r:Content xml:lang=\"fr-FR\">Indépendant</r:Content> </r:Label> </Category> </Fragment>",
    VersionDate: "2023-07-04T09:19:29.3073232Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  },
  {
    ItemType: "7e47c269-bcab-40f7-a778-af7bbc4e3d00",
    AgencyId: "fr.insee",
    Version: "1",
    Identifier: "d1ee8459-65ec-40e2-a7e3-f1809e1b1b5a",
    Item: " <Fragment xmlns=\"ddi:instance:3_3\" xmlns:r=\"ddi:reusable:3_3\"> <Category isUniversallyUnique=\"true\" versionDate=\"2023-07-04T09:19:29.3073232Z\" xmlns=\"ddi:logicalproduct:3_3\"> <r:URN>urn:ddi:fr.insee:d1ee8459-65ec-40e2-a7e3-f1809e1b1b5a:1</r:URN> <r:Agency>fr.insee</r:Agency> <r:ID>d1ee8459-65ec-40e2-a7e3-f1809e1b1b5a</r:ID> <r:Version>1</r:Version> <r:Label> <r:Content xml:lang=\"fr-FR\">Salarié</r:Content> </r:Label> </Category> </Fragment>",
    VersionDate: "2023-07-04T09:19:29.3073232Z",
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  }
];

/**
 * Initialize the items store with default data
 * This function is now async and checks if items already exist in Firestore
 */
async function initializeDefaultItems() {
  try {
    const itemsCollection = db.collection(ITEMS_COLLECTION);
    const snapshot = await itemsCollection.limit(1).get();

    // Only initialize if collection is empty
    if (snapshot.empty) {
      console.log('Initializing Firestore with default items...');
      const batch = db.batch();

      defaultItems.forEach(item => {
        const docRef = itemsCollection.doc(item.Identifier);
        batch.set(docRef, item);
      });

      await batch.commit();
      console.log(`Initialized Firestore with ${defaultItems.length} default items`);
    } else {
      console.log('Firestore already contains items, skipping initialization');
    }
  } catch (error) {
    console.error('Error initializing default items:', error);
  }
}

// Note: Initialization is now handled on-demand when the first request comes in
// to avoid cold start delays

/**
 * In-memory storage for valid tokens
 */
const validTokens = new Set();

/**
 * CORS headers configuration
 */
const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS'
};

/**
 * Generate a mock token
 */
function generateMockToken() {
  return 'mock-token-' + Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
}

/**
 * Validate token from Authorization header
 */
function validateToken(authHeader) {
  if (!authHeader) return false;
  const token = authHeader.replace('Bearer ', '');
  return validTokens.has(token);
}

/**
 * Extract item identifier from item data
 */
function getItemIdentifier(item) {
  if (item.CompositeId) {
    return `${item.CompositeId.AgencyId}:${item.CompositeId.Identifier}:${item.CompositeId.Version}`;
  }
  if (item.Identifier) {
    return item.Identifier;
  }
  return null;
}

/**
 * XML Parser instance with configuration
 */
const xmlParser = new XMLParser({
  ignoreAttributes: false,
  attributeNamePrefix: '@_',
  textNodeName: '#text'
});

/**
 * Extract label/name from XML Item content
 */
function extractLabelFromXml(xmlContent, itemType) {
  if (!xmlContent) return {};

  const labels = {};

  try {
    const parsed = xmlParser.parse(xmlContent);
    const fragment = parsed.Fragment;

    if (!fragment) return {};

    // For PhysicalInstance: extract from Citation/Title
    if (itemType === 'a51e85bb-6259-4488-8df2-f08cb43485f8') {
      const physicalInstance = fragment.PhysicalInstance;
      if (physicalInstance && physicalInstance['r:Citation'] && physicalInstance['r:Citation']['r:Title']) {
        const titleString = physicalInstance['r:Citation']['r:Title']['r:String'];
        if (titleString) {
          const lang = titleString['@_xml:lang'] || 'fr-FR';
          const value = titleString['#text'] || titleString;
          labels[lang] = value;
        }
      }
    }
    // For Variable (683889c6-f74b-4d5e-92ed-908c0a42bb2d)
    else if (itemType === '683889c6-f74b-4d5e-92ed-908c0a42bb2d') {
      const variable = fragment.Variable;
      if (variable && variable.VariableName) {
        const nameString = variable.VariableName['r:String'];
        if (nameString) {
          const lang = nameString['@_xml:lang'] || 'fr-FR';
          const value = nameString['#text'] || nameString;
          labels[lang] = value;
        }
      }
    }
    // For DataRelationship (f39ff278-8500-45fe-a850-3906da2d242b)
    else if (itemType === 'f39ff278-8500-45fe-a850-3906da2d242b') {
      const dataRelationship = fragment.DataRelationship;
      if (dataRelationship && dataRelationship.DataRelationshipName) {
        const nameString = dataRelationship.DataRelationshipName['r:String'];
        if (nameString) {
          const lang = nameString['@_xml:lang'] || 'fr-FR';
          const value = nameString['#text'] || nameString;
          labels[lang] = value;
        }
      }
    }
    // For CodeList (8b108ef8-b642-4484-9c49-f88e4bf7cf1d)
    else if (itemType === '8b108ef8-b642-4484-9c49-f88e4bf7cf1d') {
      const codeList = fragment.CodeList;
      if (codeList && codeList['r:Label']) {
        const labelContent = codeList['r:Label']['r:Content'];
        if (labelContent) {
          const lang = labelContent['@_xml:lang'] || 'fr-FR';
          const value = labelContent['#text'] || labelContent;
          labels[lang] = value;
        }
      }
    }
    // For Category (7e47c269-bcab-40f7-a778-af7bbc4e3d00)
    else if (itemType === '7e47c269-bcab-40f7-a778-af7bbc4e3d00') {
      const category = fragment.Category;
      if (category && category['r:Label']) {
        const labelContent = category['r:Label']['r:Content'];
        if (labelContent) {
          const lang = labelContent['@_xml:lang'] || 'fr-FR';
          const value = labelContent['#text'] || labelContent;
          labels[lang] = value;
        }
      }
    }
    // For Group (4bd6eef6-99df-40e6-9b11-5b8f64e5cb23)
    else if (itemType === '4bd6eef6-99df-40e6-9b11-5b8f64e5cb23') {
      const group = fragment.Group;
      if (group && group['r:Citation'] && group['r:Citation']['r:Title']) {
        const titleString = group['r:Citation']['r:Title']['r:String'];
        if (titleString) {
          const lang = titleString['@_xml:lang'] || 'fr-FR';
          const value = titleString['#text'] || titleString;
          labels[lang] = value;
        }
      }
    }
    // For StudyUnit (752a535b-b548-4fbe-97e4-f26a02d9e413)
    else if (itemType === '752a535b-b548-4fbe-97e4-f26a02d9e413') {
      const studyUnit = fragment.StudyUnit;
      if (studyUnit && studyUnit['r:Citation'] && studyUnit['r:Citation']['r:Title']) {
        const titleString = studyUnit['r:Citation']['r:Title']['r:String'];
        if (titleString) {
          const lang = titleString['@_xml:lang'] || 'fr-FR';
          const value = titleString['#text'] || titleString;
          labels[lang] = value;
        }
      }
    }
  } catch (error) {
    console.error('Error extracting label from XML:', error);
  }

  return labels;
}

/**
 * Main proxy function that handles all Colectica API endpoints
 */
export const proxy = onRequest(async (req, res) => {
  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    res.set(corsHeaders);
    res.status(200).send('');
    return;
  }

  const path = req.path;
  console.log(`Received request: ${req.method} ${path}`);

  try {
    // Route based on path
    if (path === '/token/createtoken') {
      await handleCreateToken(req, res);
    } else if (path === '/api/v1/_query') {
      await handleQuery(req, res);
    } else if (path === '/api/v1/item') {
      await handleItem(req, res);
    } else if (path === '/api/v1/items') {
      await handleResetItems(req, res);
    } else if (path.startsWith('/api/v1/ddiset/')) {
      await handleDdiset(req, res, path);
    } else {
      res.status(404).json({
        error: 'Endpoint not found',
        path: path,
        availableEndpoints: [
          'POST /token/createtoken',
          'POST /api/v1/_query',
          'POST /api/v1/item',
          'DELETE /api/v1/items',
          'GET /api/v1/ddiset/{agencyId}/{identifier}'
        ]
      });
    }
  } catch (error) {
    console.error('Mock error:', error);
    res.status(500).json({
      error: 'Internal mock error',
      details: error.message
    });
  }
});

/**
 * Handle authentication token creation (MOCKED)
 * POST /token/createtoken
 */
async function handleCreateToken(req, res) {
  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  console.log('Generating mock authentication token');

  // Generate a new mock token
  const token = generateMockToken();
  validTokens.add(token);

  // Mock response similar to real Colectica API
  res.set({
    'Content-Type': 'application/json',
    ...corsHeaders
  });
  res.status(200).json({
                           "access_token": token,
                           "expires": "2025-12-27T10:33:57.2173833Z"
                       });
}

/**
 * Handle query for items (MOCKED)
 * POST /api/v1/_query
 * Expected body format:
 * {
 *   "itemTypes": ["uuid1", "uuid2", ...]
 * }
 */
async function handleQuery(req, res) {
  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  // Validate token
  if (!validateToken(req.headers.authorization)) {
    res.status(401).json({ error: 'Unauthorized' });
    return;
  }

  const queryBody = req.body;
  console.log('Processing query request:', JSON.stringify(queryBody, null, 2));

  // Extract query parameters
  const itemTypes = queryBody.itemTypes || queryBody.ItemTypes || [];

  // Initialize default items if needed
  await initializeDefaultItems();

  // Get all items from Firestore
  let query = db.collection(ITEMS_COLLECTION);

  // Filter by ItemType if specified
  if (itemTypes.length > 0) {
    query = query.where('ItemType', 'in', itemTypes);
  }

  const snapshot = await query.get();
  const results = snapshot.docs.map(doc => doc.data());

  // Transform items to match expected response format
  const transformedResults = results.map(item => {
    // Extract name/label from XML Item content
    const extractedLabels = extractLabelFromXml(item.Item, item.ItemType);

    return {
      Summary: item.Summary || {},
      ItemName: extractedLabels,
      Label: extractedLabels,
      Description: item.Description || {},
      VersionRationale: item.VersionRationale || {},
      MetadataRank: item.MetadataRank || 0,
      RepositoryName: item.RepositoryName || null,
      IsAuthoritative: item.IsAuthoritative || false,
      Tags: item.Tags || [],
      ItemType: item.ItemType,
      AgencyId: item.AgencyId,
      Version: typeof item.Version === 'string' ? parseInt(item.Version) : (item.Version || 1),
      Identifier: item.Identifier,
      Item: null, // Don't return the XML content in query results
      Notes: item.Notes || null,
      VersionDate: item.VersionDate || '0001-01-01T00:00:00',
      VersionResponsibility: item.VersionResponsibility || null,
      IsPublished: item.IsPublished || false,
      IsDeprecated: item.IsDeprecated || false,
      IsProvisional: item.IsProvisional || false,
      ItemFormat: item.ItemFormat || '00000000-0000-0000-0000-000000000000',
      TransactionId: item.TransactionId || 0,
      VersionCreationType: item.VersionCreationType || 0
    };
  });

  // Build response
  const response = {
    Results: transformedResults,
    TotalResults: transformedResults.length,
    ReturnedResults: transformedResults.length,
    NextResult: null,
    DatabaseTime: '00:00:00.0085009',
    RepositoryTime: '00:00:00.0168683'
  };

  res.set({
    'Content-Type': 'application/json',
    ...corsHeaders
  });
  res.status(200).json(response);
}

/**
 * Handle item create/update (MOCKED)
 * POST /api/v1/item
 * Expected body format:
 * {
 *   "options": { "namedOptions": ["RegisterOrReplace"] },
 *   "Items": [
 *     {
 *       "ItemType": "uuid",
 *       "AgencyId": "fr.insee",
 *       "Version": "1",
 *       "Identifier": "uuid",
 *       "Item": "<xml>...</xml>",
 *       "VersionDate": "2024-06-03T14:29:23.4049817Z",
 *       "VersionResponsibility": "abcde",
 *       "IsPublished": false,
 *       "IsDeprecated": false,
 *       "IsProvisional": false,
 *       "ItemFormat": "uuid"
 *     }
 *   ]
 * }
 */
async function handleItem(req, res) {
  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  // Validate token
  if (!validateToken(req.headers.authorization)) {
    res.status(401).json({ error: 'Unauthorized' });
    return;
  }

  const requestBody = req.body;
  console.log('Processing item create/update request');

  // Save requestBody to debug collection for debugging
  try {
    await db.collection('debug').add({
      timestamp: new Date().toISOString(),
      endpoint: 'handleItem',
      requestBody: requestBody
    });
    console.log('Saved requestBody to debug collection');
  } catch (debugError) {
    console.error('Error saving to debug collection:', debugError);
  }

  // Validate request structure
  if (!requestBody.Items || !Array.isArray(requestBody.Items)) {
    res.status(400).json({
      error: 'Invalid request format. Expected { "Items": [...] }'
    });
    return;
  }

  const items = requestBody.Items;
  const results = [];
  let successCount = 0;
  let errorCount = 0;

  // Initialize default items if needed
  await initializeDefaultItems();

  // Process each item
  for (const item of items) {
    try {
      // Validate item has required fields
      if (!item.Identifier) {
        results.push({
          success: false,
          error: 'Item missing Identifier field',
          item: item
        });
        errorCount++;
        continue;
      }

      // Store the item using Identifier as key
      const identifier = item.Identifier;
      const docRef = db.collection(ITEMS_COLLECTION).doc(identifier);
      const doc = await docRef.get();
      const isUpdate = doc.exists;

      await docRef.set(item);

      results.push({
        success: true,
        action: isUpdate ? 'updated' : 'created',
        identifier: identifier,
        itemType: item.ItemType,
        agencyId: item.AgencyId,
        version: item.Version
      });
      successCount++;

      console.log(`Item ${isUpdate ? 'updated' : 'created'}: ${identifier}`);
    } catch (error) {
      results.push({
        success: false,
        error: error.message,
        identifier: item.Identifier
      });
      errorCount++;
    }
  }

  // Get total count from Firestore
  const snapshot = await db.collection(ITEMS_COLLECTION).count().get();
  const totalItems = snapshot.data().count;

  console.log(`Total items in store: ${totalItems}`);
  console.log(`Processed: ${successCount} successful, ${errorCount} failed`);

  // Return summary response
  res.set({
    'Content-Type': 'application/json',
    ...corsHeaders
  });
  res.status(200).json({
    success: errorCount === 0,
    totalProcessed: items.length,
    successCount: successCount,
    errorCount: errorCount,
    results: results
  });
}

/**
 * Handle items store reset (MOCKED)
 * DELETE /api/v1/items
 * Clears all items and reinitializes with default data
 */
async function handleResetItems(req, res) {
  if (req.method !== 'DELETE') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  // Validate token
  if (!validateToken(req.headers.authorization)) {
    res.status(401).json({ error: 'Unauthorized' });
    return;
  }

  console.log('Resetting items store...');

  // Get current size before clearing
  const countSnapshot = await db.collection(ITEMS_COLLECTION).count().get();
  const previousSize = countSnapshot.data().count;

  // Clear all items using batch delete
  const snapshot = await db.collection(ITEMS_COLLECTION).get();
  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.delete(doc.ref);
  });
  await batch.commit();

  // Reinitialize with default items
  await initializeDefaultItems();

  // Get new count
  const newCountSnapshot = await db.collection(ITEMS_COLLECTION).count().get();
  const currentSize = newCountSnapshot.data().count;

  const response = {
    success: true,
    message: 'Items store has been reset',
    previousItemCount: previousSize,
    currentItemCount: currentSize,
    defaultItemsRestored: defaultItems.length
  };

  console.log('Items store reset complete:', response);

  res.set({
    'Content-Type': 'application/json',
    ...corsHeaders
  });
  res.status(200).json(response);
}

/**
 * Handle DDI set retrieval (MOCKED)
 * GET /api/v1/ddiset/{agencyId}/{identifier}
 * Supports both PhysicalInstance and Group items
 */
async function handleDdiset(req, res, path) {
  if (req.method !== 'GET') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  // Validate token
  if (!validateToken(req.headers.authorization)) {
    res.status(401).json({ error: 'Unauthorized' });
    return;
  }

  // Extract agencyId and identifier from the path
  const pathParts = path.split('/').filter(p => p);
  const relevantParts = pathParts.slice(pathParts.indexOf('ddiset') + 1);

  if (relevantParts.length < 2) {
    res.status(400).json({
      error: 'Invalid path. Expected format: /api/v1/ddiset/{agencyId}/{identifier}'
    });
    return;
  }

  const agencyId = relevantParts[0];
  const identifier = relevantParts.slice(1).join('/');

  console.log(`Retrieving DDI set for: ${agencyId}/${identifier}`);

  // Initialize default items if needed
  await initializeDefaultItems();

  try {
    // Find the item by identifier
    const itemDoc = await db.collection(ITEMS_COLLECTION).doc(identifier).get();

    if (!itemDoc.exists) {
      res.status(404).json({
        error: 'Item not found',
        agencyId: agencyId,
        identifier: identifier
      });
      return;
    }

    const item = itemDoc.data();
    const itemType = item.ItemType;

    // Handle based on item type
    if (itemType === 'a51e85bb-6259-4488-8df2-f08cb43485f8') {
      // PhysicalInstance
      await handlePhysicalInstanceDdiset(req, res, item, agencyId);
    } else if (itemType === '4bd6eef6-99df-40e6-9b11-5b8f64e5cb23') {
      // Group
      await handleGroupDdiset(req, res, item, agencyId);
    } else {
      res.status(404).json({
        error: 'Item type not supported for ddiset',
        agencyId: agencyId,
        identifier: identifier,
        itemType: itemType
      });
    }
  } catch (error) {
    console.error('Error generating DDI set:', error);
    res.status(500).json({
      error: 'Error generating DDI set',
      details: error.message
    });
  }
}

/**
 * Handle DDI set for PhysicalInstance
 */
async function handlePhysicalInstanceDdiset(req, res, physicalInstance, agencyId) {
  // Parse PhysicalInstance XML to extract DataRelationship reference
  const piParsed = xmlParser.parse(physicalInstance.Item);
  const piFragment = piParsed.Fragment?.PhysicalInstance;
  const dataRelationshipRef = piFragment?.['r:DataRelationshipReference'];

  let dataRelationship = null;
  let variables = [];
  let codeLists = [];
  let categories = [];
  const codeListIds = new Set();
  const categoryIds = new Set();

  if (dataRelationshipRef) {
    const drId = dataRelationshipRef['r:ID'];

    // Find the DataRelationship
    const drDoc = await db.collection(ITEMS_COLLECTION).doc(drId).get();
    if (drDoc.exists) {
      dataRelationship = drDoc.data();

      // Parse DataRelationship XML to extract Variable references
      const drParsed = xmlParser.parse(dataRelationship.Item);
      const drFragment = drParsed.Fragment?.DataRelationship;
      const logicalRecord = drFragment?.LogicalRecord;
      const variablesInRecord = logicalRecord?.VariablesInRecord;

      if (variablesInRecord) {
        // Extract variable references
        let varRefs = variablesInRecord.VariableUsedReference;

        // Ensure it's an array
        if (!Array.isArray(varRefs)) {
          varRefs = [varRefs];
        }

        // Find all variables
        for (const varRef of varRefs) {
          const varId = varRef['r:ID'];
          const varDoc = await db.collection(ITEMS_COLLECTION).doc(varId).get();
          if (varDoc.exists) {
            const variable = varDoc.data();
            variables.push(variable);

            // Parse variable to extract CodeList references
            const varParsed = xmlParser.parse(variable.Item);
            const varFragment = varParsed.Fragment?.Variable;
            const varRepresentation = varFragment?.VariableRepresentation;
            const codeRepresentation = varRepresentation?.['r:CodeRepresentation'];

            if (codeRepresentation) {
              const codeListRef = codeRepresentation['r:CodeListReference'];
              if (codeListRef) {
                const codeListId = codeListRef['r:ID'];
                if (codeListId) {
                  codeListIds.add(codeListId);
                }
              }
            }
          }
        }
      }
    }
  }

  // Fetch all referenced CodeLists
  for (const codeListId of codeListIds) {
    const clDoc = await db.collection(ITEMS_COLLECTION).doc(codeListId).get();
    if (clDoc.exists) {
      const codeList = clDoc.data();
      codeLists.push(codeList);

      // Parse CodeList to extract Category references
      const clParsed = xmlParser.parse(codeList.Item);
      const clFragment = clParsed.Fragment?.CodeList;
      const codes = clFragment?.Code;

      if (codes) {
        const codeArray = Array.isArray(codes) ? codes : [codes];
        for (const code of codeArray) {
          const categoryRef = code['r:CategoryReference'];
          if (categoryRef) {
            const categoryId = categoryRef['r:ID'];
            if (categoryId) {
              categoryIds.add(categoryId);
            }
          }
        }
      }
    }
  }

  // Fetch all referenced Categories
  for (const categoryId of categoryIds) {
    const catDoc = await db.collection(ITEMS_COLLECTION).doc(categoryId).get();
    if (catDoc.exists) {
      categories.push(catDoc.data());
    }
  }

  // Also find all variables with the same agencyId that are not already in the list
  // This is a fallback to ensure all variables are returned even if not referenced
  const variableType = '683889c6-f74b-4d5e-92ed-908c0a42bb2d';
  const variableIds = new Set(variables.map(v => v.Identifier));

  const allItemsSnapshot = await db.collection(ITEMS_COLLECTION)
    .where('ItemType', '==', variableType)
    .where('AgencyId', '==', agencyId)
    .get();

  allItemsSnapshot.docs.forEach(doc => {
    const item = doc.data();
    if (!variableIds.has(item.Identifier)) {
      console.log(`Added unreferenced variable: ${item.Identifier}`);
    }
  });

  // Generate DDI FragmentInstance XML
  const xmlResponse = generateDDIFragmentInstance(physicalInstance, dataRelationship, variables, codeLists, categories);

  res.set({
    'Content-Type': 'application/xml',
    ...corsHeaders
  });
  res.status(200).send(xmlResponse);
}

/**
 * Handle DDI set for Group
 */
async function handleGroupDdiset(req, res, group, agencyId) {
  // Parse Group XML to extract StudyUnit references
  const groupParsed = xmlParser.parse(group.Item);
  const groupFragment = groupParsed.Fragment?.Group;

  let studyUnits = [];

  if (groupFragment) {
    // Extract StudyUnit references
    let studyUnitRefs = groupFragment['r:StudyUnitReference'];

    if (studyUnitRefs) {
      // Ensure it's an array
      if (!Array.isArray(studyUnitRefs)) {
        studyUnitRefs = [studyUnitRefs];
      }

      // Find all StudyUnits
      for (const suRef of studyUnitRefs) {
        const suId = suRef['r:ID'];
        const suDoc = await db.collection(ITEMS_COLLECTION).doc(suId).get();
        if (suDoc.exists) {
          studyUnits.push(suDoc.data());
        }
      }
    }
  }

  console.log(`Found ${studyUnits.length} StudyUnits for Group ${group.Identifier}`);

  // Generate DDI FragmentInstance XML for Group
  const xmlResponse = generateGroupDDIFragmentInstance(group, studyUnits);

  res.set({
    'Content-Type': 'application/xml',
    ...corsHeaders
  });
  res.status(200).send(xmlResponse);
}

/**
 * Generate DDI FragmentInstance XML by assembling fragments
 */
function generateDDIFragmentInstance(physicalInstance, dataRelationship, variables, codeLists, categories) {
  const agency = physicalInstance.AgencyId;
  const id = physicalInstance.Identifier;
  const version = physicalInstance.Version;

  // Start building the XML
  let xml = `<?xml version="1.0" encoding="utf-8"?>
<ddi:FragmentInstance xmlns:r="ddi:reusable:3_3" xmlns:ddi="ddi:instance:3_3">
    <ddi:TopLevelReference>
        <r:Agency>${agency}</r:Agency>
        <r:ID>${id}</r:ID>
        <r:Version>${version}</r:Version>
        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
    </ddi:TopLevelReference>`;

  // Add PhysicalInstance fragment (use the stored XML directly)
  xml += `
    ${physicalInstance.Item}`;

  // Add Variable fragments
  for (const variable of variables) {
    xml += `
    ${variable.Item}`;
  }

  // Add DataRelationship fragment
  if (dataRelationship) {
    xml += `
    ${dataRelationship.Item}`;
  }

  // Add CodeList fragments
  if (codeLists && codeLists.length > 0) {
    for (const codeList of codeLists) {
      xml += `
    ${codeList.Item}`;
    }
  }

  // Add Category fragments
  if (categories && categories.length > 0) {
    for (const category of categories) {
      xml += `
    ${category.Item}`;
    }
  }

  xml += `
</ddi:FragmentInstance>`;

  return xml;
}

/**
 * Generate DDI FragmentInstance XML for Group with StudyUnits
 */
function generateGroupDDIFragmentInstance(group, studyUnits) {
  const agency = group.AgencyId;
  const id = group.Identifier;
  const version = group.Version;

  // Start building the XML
  let xml = `<?xml version="1.0" encoding="utf-8"?>
<ddi:FragmentInstance xmlns:r="ddi:reusable:3_3" xmlns:ddi="ddi:instance:3_3">
    <ddi:TopLevelReference>
        <r:Agency>${agency}</r:Agency>
        <r:ID>${id}</r:ID>
        <r:Version>${version}</r:Version>
        <r:TypeOfObject>Group</r:TypeOfObject>
    </ddi:TopLevelReference>`;

  // Add Group fragment (use the stored XML directly)
  xml += `
    ${group.Item}`;

  // Add StudyUnit fragments
  for (const studyUnit of studyUnits) {
    xml += `
    ${studyUnit.Item}`;
  }

  xml += `
</ddi:FragmentInstance>`;

  return xml;
}
