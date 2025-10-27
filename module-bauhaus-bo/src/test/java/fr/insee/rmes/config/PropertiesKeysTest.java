package fr.insee.rmes.config;

import fr.insee.rmes.PropertiesKeys;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import static org.junit.jupiter.api.Assertions.*;

class PropertiesKeysTest {

    List<String> actual = List.of(PropertiesKeys.CORS_ALLOWED_ORIGIN,
            PropertiesKeys.STRUCTURES_COMPONENTS_BASE_URI,
            PropertiesKeys.CODE_LIST_BASE_URI,
            PropertiesKeys.STRUCTURES_BASE_URI,
            PropertiesKeys.DOCUMENTATIONS_GEO_BASE_URI,
            PropertiesKeys.LINKS_BASE_URI,
            PropertiesKeys.CONCEPTS_BASE_URI,
            PropertiesKeys.COLLECTIONS_BASE_URI,
            PropertiesKeys.OPERATIONS_BASE_URI,
            PropertiesKeys.OP_FAMILIES_BASE_URI,
            PropertiesKeys.OP_SERIES_BASE_URI,
            PropertiesKeys.PRODUCTS_BASE_URI,
            PropertiesKeys.DOCUMENTATIONS_BASE_URI,
            PropertiesKeys.DOCUMENTS_BASE_URI,
            PropertiesKeys.DATASET_BASE_URI,
            PropertiesKeys.DISTRIBUTION_BASE_URI,
            PropertiesKeys.BASE_URI_GESTION,
            PropertiesKeys.BASE_URI_PUBLICATION
    );

    @Test
    void shouldCheckThatThereAreNoDuplicates(){
        SortedSet<String> set = new TreeSet<>(actual);
        boolean existDuplicates = set.size()!= actual.size();
        assertFalse(existDuplicates);
    }

    @Test
    void shouldCheckPropertiesHaveTheSamePrefixe(){
        String prefixe ="fr.insee.rmes.bauhaus." ;
        int numberOfPropertiesWithTheSamePrefixe = 0;
        for(String element : actual){
            if(element.startsWith(prefixe)){
                numberOfPropertiesWithTheSamePrefixe =numberOfPropertiesWithTheSamePrefixe +1;
            }
        }
        assertEquals(numberOfPropertiesWithTheSamePrefixe ,actual.size());
    }

    @Test
    void shouldCheckPropertiesHaveNotTheSameSuffix(){
        String suffix =".baseURI" ;
        int numberOfPropertiesWithTheSameSuffix = 0;
        for(String element : actual){
            if(element.endsWith(suffix)){
                numberOfPropertiesWithTheSameSuffix =numberOfPropertiesWithTheSameSuffix +1;
            }
        }
        assertNotEquals(numberOfPropertiesWithTheSameSuffix ,actual.size());
    }





}