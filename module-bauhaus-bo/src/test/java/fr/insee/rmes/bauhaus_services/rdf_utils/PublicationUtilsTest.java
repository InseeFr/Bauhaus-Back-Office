package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.Constants;
import fr.insee.rmes.rdf_utils.RepositoryGestion;
import fr.insee.rmes.modules.commons.domain.model.ValidationStatus;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.base.InternedIRI;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static fr.insee.rmes.bauhaus_services.rdf_utils.PublicationUtils.stringEndsWithItemFromList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicationUtilsTest {

    @Mock
    RepositoryGestion repositoryGestion;

    @Mock
    RepositoryPublication repositoryPublication;


    @Test
    void shouldTranformBaseURIToPublishWithoutConvertToUriFormat() {

        Resource myResource1 = new InternedIRI("myNamespace", "mylocalName");
        Resource myResource2 = new InternedIRI("gestion.baseURI", ".mylocalName");
        PublicationUtils myPublicationUtils = new PublicationUtils("gestion.baseURI.", "publication.baseURI.",repositoryGestion,repositoryPublication);

        boolean myResponse1=!myResource1.toString().contains(myPublicationUtils.baseUriGestion());
        String myResponse2 = myResource2.toString().replace(myPublicationUtils.baseUriGestion(), myPublicationUtils.baseUriPublication());

        assertTrue(myResponse1 && ("publication.baseURI.mylocalName").equals(myResponse2));
    }

    @Test
    void shouldTVerifyStringEndsWithItemFromList() {
        String inputStr = "Bauhaus-Back-Office";
        String[] items1 = { "Office", "pdf", "fr" };
        String[] items2 = { "en", "pdf", "fr" };
        boolean response1= stringEndsWithItemFromList(inputStr, items1);
        boolean response2= !stringEndsWithItemFromList(inputStr, items2);
        assertTrue(response1 && response2);

    }

    @Test
    void testIsUnpublished() {
        String unpublishedStatus = ValidationStatus.UNPUBLISHED.getValue();
        assertTrue(PublicationUtils.isUnublished(unpublishedStatus));

        String undefinedStatus = Constants.UNDEFINED;
        assertTrue(PublicationUtils.isUnublished(undefinedStatus));

        String otherStatus = "OTHER_STATUS";
        assertFalse(PublicationUtils.isUnublished(otherStatus));

        assertFalse(PublicationUtils.isUnublished(null));
    }
}