/**
 * Script de transformation du fichier data.json en items DDI XML
 * pour le mock Colectica
 */
import { readFileSync, writeFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Lire le fichier data.json
const dataPath = join(__dirname, '..', 'data.json');
const data = JSON.parse(readFileSync(dataPath, 'utf-8'));

const items = [];

/**
 * Escape XML special characters
 */
function escapeXml(str) {
  if (!str) return '';
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

/**
 * Generate Category DDI XML
 */
function generateCategoryXml(category) {
  const labelFr = category.Label?.['fr-FR'] || category.ItemName?.['fr-FR'] || '';
  const labelEn = category.Label?.['en-IE'] || '';

  let labelContent = '';
  if (labelFr) {
    labelContent += `<r:Content xml:lang="fr-FR">${escapeXml(labelFr)}</r:Content>`;
  }
  if (labelEn) {
    labelContent += ` <r:Content xml:lang="en-IE">${escapeXml(labelEn)}</r:Content>`;
  }

  return ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <Category isUniversallyUnique="true" versionDate="${category.VersionDate || new Date().toISOString()}" xmlns="ddi:logicalproduct:3_3"> <r:URN>urn:ddi:${category.AgencyId}:${category.Identifier}:${category.Version}</r:URN> <r:Agency>${category.AgencyId}</r:Agency> <r:ID>${category.Identifier}</r:ID> <r:Version>${category.Version}</r:Version> <r:Label> ${labelContent} </r:Label> </Category> </Fragment>`;
}

/**
 * Generate Code DDI XML fragment (for inclusion in CodeList)
 */
function generateCodeXmlFragment(code) {
  return `<Code isUniversallyUnique="true"> <r:URN>urn:ddi:${code.AgencyId}:${code.Identifier}:1</r:URN> <r:Agency>${code.AgencyId}</r:Agency> <r:ID>${code.Identifier}</r:ID> <r:Version>1</r:Version> <r:CategoryReference> <r:Agency>${code.Category.AgencyId}</r:Agency> <r:ID>${code.Category.Identifier}</r:ID> <r:Version>${code.Category.Version}</r:Version> <r:TypeOfObject>Category</r:TypeOfObject> </r:CategoryReference> <r:Value>${escapeXml(code.Value)}</r:Value> </Code>`;
}

/**
 * Generate CodeList DDI XML
 */
function generateCodeListXml(codeList, codes) {
  const labelFr = codeList.Label?.['fr-FR'] || codeList.ItemName?.['fr-FR'] || '';

  const codeFragments = codes.map(code => generateCodeXmlFragment(code)).join(' , ');

  return ` <Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3"> <CodeList isUniversallyUnique="true" versionDate="${codeList.VersionDate || new Date().toISOString()}" xmlns="ddi:logicalproduct:3_3"> <r:URN>urn:ddi:${codeList.AgencyId}:${codeList.Identifier}:${codeList.Version}</r:URN> <r:Agency>${codeList.AgencyId}</r:Agency> <r:ID>${codeList.Identifier}</r:ID> <r:Version>${codeList.Version}</r:Version> <r:Label> <r:Content xml:lang="fr-FR">${escapeXml(labelFr)}</r:Content> </r:Label> ${codeFragments} </CodeList> </Fragment>`;
}

/**
 * Create an item in the mock format
 */
function createItem(itemType, agencyId, version, identifier, xmlContent, versionDate) {
  return {
    ItemType: itemType,
    AgencyId: agencyId,
    Version: String(version),
    Identifier: identifier,
    Item: xmlContent,
    VersionDate: versionDate || new Date().toISOString(),
    VersionResponsibility: "abcde",
    IsPublished: false,
    IsDeprecated: false,
    IsProvisional: false,
    ItemFormat: "DC337820-AF3A-4C0B-82F9-CF02535CDE83"
  };
}

// Process Categories first
console.log('Processing categories...');
const categoryItemType = '7e47c269-bcab-40f7-a778-af7bbc4e3d00';

for (const code of data.Codes) {
  if (code.Category) {
    const categoryXml = generateCategoryXml(code.Category);
    items.push(createItem(
      categoryItemType,
      code.Category.AgencyId,
      code.Category.Version,
      code.Category.Identifier,
      categoryXml,
      code.Category.VersionDate
    ));
  }
}
console.log(`Generated ${items.length} category items`);

// Process CodeList
console.log('Processing CodeList...');
const codeListItemType = '8b108ef8-b642-4484-9c49-f88e4bf7cf1d';
const codeListXml = generateCodeListXml(data, data.Codes);

items.push(createItem(
  codeListItemType,
  data.AgencyId,
  data.Version,
  data.Identifier,
  codeListXml,
  data.VersionDate
));

console.log(`Total items generated: ${items.length}`);

// Write output file
const outputPath = join(__dirname, '..', 'functions', 'nafr2-data.js');
const output = `/**
 * NAF rév. 2 CodeList and Categories data
 * Auto-generated from data.json
 */
export const nafr2Items = ${JSON.stringify(items, null, 2)};
`;

writeFileSync(outputPath, output, 'utf-8');
console.log(`Output written to: ${outputPath}`);