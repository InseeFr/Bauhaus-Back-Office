/**
 * Unit tests for Colectica API mock functions
 */
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { XMLParser } from 'fast-xml-parser';

// Mock data for testing
const mockPhysicalInstanceXml = `
<Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
    <PhysicalInstance isUniversallyUnique="true" versionDate="2024-06-03T14:29:23.4049817Z" xmlns="ddi:physicalinstance:3_3">
        <r:URN>urn:ddi:fr.insee:9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd:1</r:URN>
        <r:Agency>fr.insee</r:Agency>
        <r:ID>9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd</r:ID>
        <r:Version>1</r:Version>
        <r:Citation>
            <r:Title>
                <r:String xml:lang="fr-FR">Fichier thl-CASD</r:String>
            </r:Title>
        </r:Citation>
        <r:DataRelationshipReference>
            <r:Agency>fr.insee</r:Agency>
            <r:ID>d8283793-e88d-4cc7-a697-2951054e9a3a</r:ID>
            <r:Version>1</r:Version>
            <r:TypeOfObject>DataRelationship</r:TypeOfObject>
        </r:DataRelationshipReference>
    </PhysicalInstance>
</Fragment>`;

const mockVariableXml = `
<Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
    <Variable isUniversallyUnique="true" versionDate="2024-06-03T13:35:37.9342777Z" xmlns="ddi:logicalproduct:3_3">
        <r:URN>urn:ddi:fr.insee:3f61240b-b035-4349-ab3d-6392dad0fc7d:1</r:URN>
        <r:Agency>fr.insee</r:Agency>
        <r:ID>3f61240b-b035-4349-ab3d-6392dad0fc7d</r:ID>
        <r:Version>1</r:Version>
        <VariableName>
            <r:String xml:lang="fr-FR">ACTOCCUPE</r:String>
        </VariableName>
        <r:Label>
            <r:Content xml:lang="fr-FR">Actif occupé</r:Content>
        </r:Label>
    </Variable>
</Fragment>`;

const mockDataRelationshipXml = `
<Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
    <DataRelationship isUniversallyUnique="true" versionDate="2024-06-03T14:29:23.4049817Z" xmlns="ddi:logicalproduct:3_3">
        <r:URN>urn:ddi:fr.insee:d8283793-e88d-4cc7-a697-2951054e9a3a:1</r:URN>
        <r:Agency>fr.insee</r:Agency>
        <r:ID>d8283793-e88d-4cc7-a697-2951054e9a3a</r:ID>
        <r:Version>1</r:Version>
        <DataRelationshipName>
            <r:String xml:lang="fr-FR">Dessin de fichier thl-CASD</r:String>
        </DataRelationshipName>
        <LogicalRecord isUniversallyUnique="true">
            <r:URN>urn:ddi:fr.insee:8a66676f-ff77-474b-a9e6-4bd9ae7bdfab:1</r:URN>
            <r:Agency>fr.insee</r:Agency>
            <r:ID>8a66676f-ff77-474b-a9e6-4bd9ae7bdfab</r:ID>
            <r:Version>1</r:Version>
            <LogicalRecordName>
                <r:String xml:lang="fr-FR">Dessin de fichier thl-CASD</r:String>
            </LogicalRecordName>
            <VariablesInRecord>
                <VariableUsedReference>
                    <r:Agency>fr.insee</r:Agency>
                    <r:ID>3f61240b-b035-4349-ab3d-6392dad0fc7d</r:ID>
                    <r:Version>1</r:Version>
                    <r:TypeOfObject>Variable</r:TypeOfObject>
                </VariableUsedReference>
            </VariablesInRecord>
        </LogicalRecord>
    </DataRelationship>
</Fragment>`;

const mockCodeListXml = `
<Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
    <CodeList isUniversallyUnique="true" versionDate="2023-07-04T09:19:29.3053289Z" xmlns="ddi:logicalproduct:3_3">
        <r:URN>urn:ddi:fr.insee:2a22ba00-a977-4a61-a582-99025c6b0582:1</r:URN>
        <r:Agency>fr.insee</r:Agency>
        <r:ID>2a22ba00-a977-4a61-a582-99025c6b0582</r:ID>
        <r:Version>1</r:Version>
        <r:Label>
            <r:Content xml:lang="fr-FR">Liste des statuts professionnels</r:Content>
        </r:Label>
    </CodeList>
</Fragment>`;

const mockCategoryXml = `
<Fragment xmlns="ddi:instance:3_3" xmlns:r="ddi:reusable:3_3">
    <Category isUniversallyUnique="true" versionDate="2023-07-04T09:19:29.3073232Z" xmlns="ddi:logicalproduct:3_3">
        <r:URN>urn:ddi:fr.insee:d597f327-773f-4ae8-852f-ae04166827da:1</r:URN>
        <r:Agency>fr.insee</r:Agency>
        <r:ID>d597f327-773f-4ae8-852f-ae04166827da</r:ID>
        <r:Version>1</r:Version>
        <r:Label>
            <r:Content xml:lang="fr-FR">Indépendant</r:Content>
        </r:Label>
    </Category>
</Fragment>`;

// Create the XML parser instance
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
  } catch (error) {
    console.error('Error extracting label from XML:', error);
  }

  return labels;
}

describe('extractLabelFromXml', () => {
  it('should extract title from PhysicalInstance XML', () => {
    const result = extractLabelFromXml(mockPhysicalInstanceXml, 'a51e85bb-6259-4488-8df2-f08cb43485f8');
    expect(result).toEqual({ 'fr-FR': 'Fichier thl-CASD' });
  });

  it('should extract name from Variable XML', () => {
    const result = extractLabelFromXml(mockVariableXml, '683889c6-f74b-4d5e-92ed-908c0a42bb2d');
    expect(result).toEqual({ 'fr-FR': 'ACTOCCUPE' });
  });

  it('should extract name from DataRelationship XML', () => {
    const result = extractLabelFromXml(mockDataRelationshipXml, 'f39ff278-8500-45fe-a850-3906da2d242b');
    expect(result).toEqual({ 'fr-FR': 'Dessin de fichier thl-CASD' });
  });

  it('should extract label from CodeList XML', () => {
    const result = extractLabelFromXml(mockCodeListXml, '8b108ef8-b642-4484-9c49-f88e4bf7cf1d');
    expect(result).toEqual({ 'fr-FR': 'Liste des statuts professionnels' });
  });

  it('should extract label from Category XML', () => {
    const result = extractLabelFromXml(mockCategoryXml, '7e47c269-bcab-40f7-a778-af7bbc4e3d00');
    expect(result).toEqual({ 'fr-FR': 'Indépendant' });
  });

  it('should return empty object for null XML', () => {
    const result = extractLabelFromXml(null, 'a51e85bb-6259-4488-8df2-f08cb43485f8');
    expect(result).toEqual({});
  });

  it('should return empty object for invalid XML', () => {
    const result = extractLabelFromXml('<invalid>xml</invalid>', 'a51e85bb-6259-4488-8df2-f08cb43485f8');
    expect(result).toEqual({});
  });

  it('should return empty object for unknown item type', () => {
    const result = extractLabelFromXml(mockPhysicalInstanceXml, 'unknown-type');
    expect(result).toEqual({});
  });
});

describe('generateDDIFragmentInstance', () => {
  function generateDDIFragmentInstance(physicalInstance, dataRelationship, variables) {
    const agency = physicalInstance.AgencyId;
    const id = physicalInstance.Identifier;
    const version = physicalInstance.Version;

    let xml = `<?xml version="1.0" encoding="utf-8"?>
<ddi:FragmentInstance xmlns:r="ddi:reusable:3_3" xmlns:ddi="ddi:instance:3_3">
    <ddi:TopLevelReference>
        <r:Agency>${agency}</r:Agency>
        <r:ID>${id}</r:ID>
        <r:Version>${version}</r:Version>
        <r:TypeOfObject>PhysicalInstance</r:TypeOfObject>
    </ddi:TopLevelReference>`;

    xml += `
    ${physicalInstance.Item}`;

    for (const variable of variables) {
      xml += `
    ${variable.Item}`;
    }

    if (dataRelationship) {
      xml += `
    ${dataRelationship.Item}`;
    }

    xml += `
</ddi:FragmentInstance>`;

    return xml;
  }

  it('should generate DDI FragmentInstance with PhysicalInstance only', () => {
    const physicalInstance = {
      AgencyId: 'fr.insee',
      Identifier: '9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd',
      Version: '1',
      Item: mockPhysicalInstanceXml
    };

    const result = generateDDIFragmentInstance(physicalInstance, null, []);

    expect(result).toContain('<?xml version="1.0" encoding="utf-8"?>');
    expect(result).toContain('<ddi:FragmentInstance');
    expect(result).toContain('<ddi:TopLevelReference>');
    expect(result).toContain('<r:Agency>fr.insee</r:Agency>');
    expect(result).toContain('<r:ID>9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd</r:ID>');
    expect(result).toContain('<r:TypeOfObject>PhysicalInstance</r:TypeOfObject>');
    expect(result).toContain(mockPhysicalInstanceXml);
    expect(result).toContain('</ddi:FragmentInstance>');
  });

  it('should generate DDI FragmentInstance with PhysicalInstance, Variables, and DataRelationship', () => {
    const physicalInstance = {
      AgencyId: 'fr.insee',
      Identifier: '9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd',
      Version: '1',
      Item: mockPhysicalInstanceXml
    };

    const variable = {
      Item: mockVariableXml
    };

    const dataRelationship = {
      Item: mockDataRelationshipXml
    };

    const result = generateDDIFragmentInstance(physicalInstance, dataRelationship, [variable]);

    expect(result).toContain(mockPhysicalInstanceXml);
    expect(result).toContain(mockVariableXml);
    expect(result).toContain(mockDataRelationshipXml);
  });

  it('should handle multiple variables', () => {
    const physicalInstance = {
      AgencyId: 'fr.insee',
      Identifier: '9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd',
      Version: '1',
      Item: mockPhysicalInstanceXml
    };

    const variable1 = { Item: mockVariableXml };
    const variable2 = { Item: mockVariableXml.replace('ACTOCCUPE', 'VARIABLE2') };

    const result = generateDDIFragmentInstance(physicalInstance, null, [variable1, variable2]);

    expect(result).toContain('ACTOCCUPE');
    expect(result).toContain('VARIABLE2');
  });
});

describe('Item transformation for query response', () => {
  it('should transform item with all required fields', () => {
    const item = {
      ItemType: 'a51e85bb-6259-4488-8df2-f08cb43485f8',
      AgencyId: 'fr.insee',
      Version: '1',
      Identifier: '9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd',
      Item: mockPhysicalInstanceXml,
      VersionDate: '2024-06-03T14:29:23.4049817Z',
      VersionResponsibility: 'abcde',
      IsPublished: false,
      IsDeprecated: false,
      IsProvisional: false,
      ItemFormat: 'DC337820-AF3A-4C0B-82F9-CF02535CDE83'
    };

    const extractedLabels = extractLabelFromXml(item.Item, item.ItemType);

    const transformed = {
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
      Item: null,
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

    expect(transformed.ItemName).toEqual({ 'fr-FR': 'Fichier thl-CASD' });
    expect(transformed.Label).toEqual({ 'fr-FR': 'Fichier thl-CASD' });
    expect(transformed.Version).toBe(1);
    expect(transformed.Item).toBeNull();
    expect(transformed.AgencyId).toBe('fr.insee');
    expect(transformed.Identifier).toBe('9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd');
  });

  it('should handle numeric version', () => {
    const item = {
      ItemType: 'a51e85bb-6259-4488-8df2-f08cb43485f8',
      AgencyId: 'fr.insee',
      Version: 2,
      Identifier: '9a7f1abd-10ec-48f3-975f-fcfedb7dc4cd',
      Item: mockPhysicalInstanceXml
    };

    const extractedLabels = extractLabelFromXml(item.Item, item.ItemType);

    const transformed = {
      Version: typeof item.Version === 'string' ? parseInt(item.Version) : (item.Version || 1)
    };

    expect(transformed.Version).toBe(2);
  });
});

describe('XML parsing for DataRelationship references', () => {
  it('should extract DataRelationship reference from PhysicalInstance', () => {
    const parsed = xmlParser.parse(mockPhysicalInstanceXml);
    const fragment = parsed.Fragment?.PhysicalInstance;
    const dataRelationshipRef = fragment?.['r:DataRelationshipReference'];

    expect(dataRelationshipRef).toBeDefined();
    expect(dataRelationshipRef['r:ID']).toBe('d8283793-e88d-4cc7-a697-2951054e9a3a');
    expect(dataRelationshipRef['r:Agency']).toBe('fr.insee');
    expect(dataRelationshipRef['r:Version']).toBe('1');
  });

  it('should extract Variable references from DataRelationship', () => {
    const parsed = xmlParser.parse(mockDataRelationshipXml);
    const fragment = parsed.Fragment?.DataRelationship;
    const logicalRecord = fragment?.LogicalRecord;
    const variablesInRecord = logicalRecord?.VariablesInRecord;

    expect(variablesInRecord).toBeDefined();
    expect(variablesInRecord.VariableUsedReference).toBeDefined();

    const varRef = variablesInRecord.VariableUsedReference;
    expect(varRef['r:ID']).toBe('3f61240b-b035-4349-ab3d-6392dad0fc7d');
  });
});
