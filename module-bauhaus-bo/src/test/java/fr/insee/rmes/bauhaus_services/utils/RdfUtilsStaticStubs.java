package fr.insee.rmes.bauhaus_services.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.utils.DateUtils;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.mockito.MockedStatic;

/**
 * Bouchons statiques partagés par les tests des services qui construisent un modèle RDF de jeu de données ou de
 * distribution sous {@code mockStatic(RdfUtils.class)} : les méthodes bouchonnées ici appellent la vraie
 * implémentation, les autres rendent la valeur par défaut du mock.
 */
public final class RdfUtilsStaticStubs {

    /** Date courante figée rendue par {@link DateUtils#getCurrentDate()}. */
    public static final String CURRENT_DATE = "2023-10-19T11:44:23.335590";

    private RdfUtilsStaticStubs() {}

    /**
     * Fabrique d'IRI, IRI de série {@code 2}, littéraux texte et date-heure, triplets texte et date-heure : le socle
     * commun à la création et à la mise à jour.
     */
    public static void callRealLiteralAndTripleBuilders(MockedStatic<RdfUtils> rdfUtilsMock) {
        rdfUtilsMock.when(() -> RdfUtils.createIRI(any())).thenCallRealMethod();
        rdfUtilsMock
                .when(() -> RdfUtils.seriesIRI("2"))
                .thenReturn(SimpleValueFactory.getInstance().createIRI("http://seriesIRI/2"));
        rdfUtilsMock.when(() -> RdfUtils.setLiteralString(anyString())).thenCallRealMethod();
        rdfUtilsMock
                .when(() -> RdfUtils.setLiteralString(anyString(), anyString()))
                .thenCallRealMethod();
        rdfUtilsMock.when(() -> RdfUtils.setLiteralDateTime(any())).thenCallRealMethod();
        rdfUtilsMock
                .when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any(), any()))
                .thenCallRealMethod();
        rdfUtilsMock
                .when(() -> RdfUtils.addTripleString(any(), any(), any(), any(), any()))
                .thenCallRealMethod();
        rdfUtilsMock
                .when(() -> RdfUtils.addTripleDateTime(any(), any(), any(), any(), any()))
                .thenCallRealMethod();
    }

    /** {@link DateUtils#getCurrentDate()} rend {@link #CURRENT_DATE}. */
    public static void freezeCurrentDate(MockedStatic<DateUtils> dateUtilsMock) {
        dateUtilsMock.when(DateUtils::getCurrentDate).thenReturn(CURRENT_DATE);
    }
}
