package fr.insee.rmes.config;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.persistance.sparql_queries.GenericQueries;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static fr.insee.rmes.config.PropertiesKeys.*;


@Component
public class Config {


    /******************************************************/
    /** GLOBAL CONF 	***********************************/
    /******************************************************/


    @Value("${fr.insee.rmes.bauhaus.lg1}")
    private String lg1;
    @Value("${fr.insee.rmes.bauhaus.lg2}")
    private String lg2;

    @Value("${fr.insee.rmes.bauhaus.baseGraph}")
    private String baseGraph;

    @Value("${fr.insee.rmes.bauhaus.per_page}")
    private int perPage;

    /******************************************************/
    /** DATABASES		***********************************/
    /******************************************************/
    @Value("${fr.insee.rmes.bauhaus.sesame.gestion.baseURI}")
    private String baseUriGestion;

    @Value("${fr.insee.rmes.bauhaus.concepts.graph}") //Getter with baseGraph
    private String conceptsGraph;
    @Value("${fr.insee.rmes.bauhaus.concepts.scheme}")
    private String conceptsScheme;


    /******************************************************/
    /** CLASSIFICATIONS	***********************************/
    /******************************************************/
    @Value("${fr.insee.rmes.bauhaus.classifications.families.graph}")     //Getter with baseGraph
    private String classifFamiliesGraph;

    /******************************************************/
    /** OPERATIONS		***********************************/
    /******************************************************/
    @Value("${fr.insee.rmes.bauhaus.operations.graph}")    //Getter with baseGraph
    private String operationsGraph;


    @Value("${fr.insee.rmes.bauhaus.documentations.graph}")    //Getter with baseGraph
    private String documentationsGraph;
    @Value("${fr.insee.rmes.bauhaus.documentations.msd.graph}")    //Getter with baseGraph
    private String msdGraph;
    @Value("${fr.insee.rmes.bauhaus.documentations.concepts.graph}")    //Getter with baseGraph
    private String msdConceptsGraph;
    @Value("${fr.insee.rmes.bauhaus.documentation.geographie.graph}")    //Getter with baseGraph
    private String documentationsGeoGraph;

    @Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg1}")
    private String documentationsTitlePrefixLg1;
    @Value("${fr.insee.rmes.bauhaus.documentation.titlePrefixLg2}")
    private String documentationsTitlePrefixLg2;
    @Value("${" + LINKS_BASE_URI + "}")
    private String linksBaseUri;

    @Value("${fr.insee.rmes.bauhaus.documents.graph}")    //Getter with baseGraph
    private String documentsGraph;
    @Value("${fr.insee.rmes.bauhaus.storage.document.gestion}")
    private String documentsStorageGestion;
    @Value("${fr.insee.rmes.bauhaus.storage.document.publication}")
    private String documentsStoragePublicationExt;
    @Value("${fr.insee.rmes.bauhaus.storage.document.publication.interne}")
    private String documentsStoragePublicationInt;
    @Value("${fr.insee.web4g.baseURL}")
    private String documentsBaseUrl;

    @Value("${fr.insee.rmes.bauhaus.products.graph}")//Getter with baseGraph
    private String productsGraph;
    @Value("${fr.insee.rmes.bauhaus.products.baseURI}")
    private String productsBaseUri;

    /******************************************************/
    /** STRUCTURES		***********************************/
    /******************************************************/
    @Value("${fr.insee.rmes.bauhaus.structures.graph}")    //Getter with baseGraph
    private String structuresGraph;

    @Value("${fr.insee.rmes.bauhaus.structures.components.graph}")    //Getter with baseGraph
    private String structuresComponentsGraph;

    /******************************************************/
    /** CODE LISTS		***********************************/
    /******************************************************/
    @Value("${fr.insee.rmes.bauhaus.codelists.graph}")    //Getter with baseGraph
    private String codeListsGraph;

    /******************************************************/
    /** ORGANIZATIONS	***********************************/
    /******************************************************/
    @Value("${fr.insee.rmes.bauhaus.organisations.graph}") //Getter with baseGraph
    private String organizationsGraph;
    @Value("${fr.insee.rmes.bauhaus.insee.graph}") //Getter with baseGraph
    private String orgInseeGraph;


    /******************************************************/
    /** GEOGRAPHY		***********************************/
    /******************************************************/
    @Value("${fr.insee.rmes.bauhaus.geographie.graph}")     //Getter with baseGraph
    private String geographyGraph;

    @Value("${" + CODE_LIST_BASE_URI + "}")
    private String codeListBaseUri;
    @Value("${" + DOCUMENTS_BASE_URI + "}")
    private String documentsBaseUri;


    /******************************************************/
    /** INIT STATIC		***********************************/
    /******************************************************/
    @PostConstruct
    private void init() {
        GenericQueries.setConfig(this);
        RdfUtils.setConfig(this);
    }

    /******************************************************/
    /** GETTERS 		***********************************/
    /******************************************************/

    public String getLg1() {
        return lg1;
    }

    public String getLg2() {
        return lg2;
    }

    public String getBaseGraph() {
        return baseGraph;
    }

    public String getBaseUriGestion() {
        return baseUriGestion;
    }

    public String getConceptsGraph() {
        return baseGraph + conceptsGraph;
    }

    public String getConceptsScheme() {
        return conceptsScheme;
    }

    public String getClassifFamiliesGraph() {
        return baseGraph + classifFamiliesGraph;
    }

    public String getOperationsGraph() {
        return baseGraph + operationsGraph;
    }

    public String getDocumentationsGraph() {
        return baseGraph + documentationsGraph;
    }

    public String getMsdGraph() {
        return baseGraph + msdGraph;
    }

    public String getMsdConceptsGraph() {
        return baseGraph + msdConceptsGraph;
    }

    public String getDocumentationsGeoGraph() {
        return baseGraph + documentationsGeoGraph;
    }

    public String getDocumentationsTitlePrefixLg1() {
        return documentationsTitlePrefixLg1;
    }

    public String getDocumentationsTitlePrefixLg2() {
        return documentationsTitlePrefixLg2;
    }

    public String getDocumentsGraph() {
        return baseGraph + documentsGraph;
    }

    public String getDocumentsStorageGestion() {
        return documentsStorageGestion;
    }

    public String getDocumentsStoragePublicationExterne() {
        return documentsStoragePublicationExt;
    }

    public String getDocumentsStoragePublicationInterne() {
        return documentsStoragePublicationInt;
    }

    public String getDocumentsBaseurl() {
        return documentsBaseUrl.trim();
    }

    public String getProductsGraph() {
        return baseGraph + productsGraph;
    }

    public String getProductsBaseUri() {
        return productsBaseUri;
    }

    public String getStructuresGraph() {
        return baseGraph + structuresGraph;
    }

    public String getStructuresComponentsGraph() {
        return baseGraph + structuresComponentsGraph;
    }

    public String getCodeListGraph() {
        return baseGraph + codeListsGraph;
    }

    public String getOrganizationsGraph() {
        return baseGraph + organizationsGraph;
    }

    public String getOrgInseeGraph() {
        return baseGraph + orgInseeGraph;
    }

    public String getGeographyGraph() {
        return baseGraph + geographyGraph;
    }

    public String getCodeListBaseUri() {
        return this.codeListBaseUri;
    }

    public String getLinksBaseUri() {
        return this.linksBaseUri;
    }

    public String getDocumentsBaseUri() {
        return this.documentsBaseUri;
    }

    public int getPerPage() {
        return perPage;
    }
}