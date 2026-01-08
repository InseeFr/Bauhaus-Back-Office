package fr.insee.rmes.bauhaus_services.concepts;

import fr.insee.rmes.Stubber;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.config.ConfigStub;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.model.concepts.CollectionForExport;
import fr.insee.rmes.graphdb.GenericQueries;
import fr.insee.rmes.utils.XMLUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptsCollectionServiceImplTest {
    @Mock
    RepositoryGestion repoGestion;


    @BeforeAll
    static void initGenericQueries(){
        GenericQueries.setConfig(new ConfigStub());
    }

    @Test
    void shouldConvertCollectionInXml() {

        JSONArray members = new JSONArray();
        members.put(new JSONObject().put("id", "ID2").put("creator", "CREATOR").put("prefLabelLg1", "PREFLABELLG1").put("prefLabelLg2", "PREFLABELLG2").put("defCourteLg1", "DEFCOURTELG1").put("defCourteLg2", "DEFCOURTELG2").put("defLongueLg1", "DEFLONGUELG1").put("defLongueLg2", "DEFLONGUELG2").put("isValidated", "ISVALIDATED").put("editorialNoteLg1", "EDITORIALNOTELG1").put("editorialNoteLg2", "EDITORIALNOTELG2").put("createdA", "CREATEA").put("modifiedA", "MODIFIEDA"));
        CollectionForExport collectionForExport = new CollectionForExport();
        collectionForExport.addMembers(members);

        String collectionXml = XMLUtils.produceXMLResponse(collectionForExport);
        Map<String,String> xmlContent = new HashMap<>();
        xmlContent.put("collectionFile",  collectionXml.replace("CollectionForExport", "Collection"));

        String collectionFile="{collectionFile=<Collection><id/><prefLabelLg1/><prefLabelLg2/><creator/><contributor/><created/><modified/><isValidated/><membersLg><membersLg><id>ID2</id><creator>CREATOR</creator><prefLabelLg1>PREFLABELLG1</prefLabelLg1><prefLabelLg2>PREFLABELLG2</prefLabelLg2><defCourteLg1>DEFCOURTELG1</defCourteLg1><defCourteLg2>DEFCOURTELG2</defCourteLg2><defLongueLg1>DEFLONGUELG1</defLongueLg1><defLongueLg2>DEFLONGUELG2</defLongueLg2><isValidated>Provisoire</isValidated><editorialNoteLg1>EDITORIALNOTELG1</editorialNoteLg1><editorialNoteLg2>EDITORIALNOTELG2</editorialNoteLg2><created/><modified/></membersLg></membersLg><descriptionLg1/><descriptionLg2/></Collection>}";

        assertEquals(collectionFile,xmlContent.toString());
    }


}