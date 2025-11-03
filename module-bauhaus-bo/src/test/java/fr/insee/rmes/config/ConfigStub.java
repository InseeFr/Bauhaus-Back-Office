package fr.insee.rmes.config;

import fr.insee.rmes.Config;

public class ConfigStub extends Config {

    @Override
    public String getLg2() {
        return "en";
    }

    @Override
    public String getLg1() {
        return "fr";
    }

    @Override
    public String getOperationsGraph() {
        return "http://rdf.insee.fr/graphes/operations";
    }

    @Override
    public String getOrganizationsGraph() {
        return "http://rdf.insee.fr/graphes/organisations";
    }

    @Override
    public String getProductsGraph() {
        return "http://rdf.insee.fr/graphes/produits";
    }


    @Override
    public String getOrgInseeGraph() {
        return "http://rdf.insee.fr/graphes/organisations/insee";
    }

    @Override
    public String getStructuresComponentsGraph() {
        return "http://rdf.insee.fr/graphes/composants";
    }

    @Override
    public String getStructuresGraph() {
        return "http://rdf.insee.fr/graphes/structures";
    }

    @Override
    public String getCodeListGraph() {
        return "http://rdf.insee.fr/graphes/codes";
    }

    @Override
    public String getDocumentationsGraph(){
        return "http://rdf.insee.fr/graphes/qualite/rapport";
    }

    @Override
    public String getMsdGraph(){
        return "http://rdf.insee.fr/graphes/qualite/simsv2fr";
    }

    @Override
    public String getMsdConceptsGraph(){
        return "http://rdf.insee.fr/graphes/concepts/qualite";
    }

    @Override
    public String getConceptsGraph() {
        return "http://rdf.insee.fr/graphes/concepts/";
    }

    @Override
    public String getBaseUriGestion() {
        return "http://bauhaus/";
    }

    @Override
    public String getConceptsScheme() {
        return "concepts/definitions/scheme";
    }

    @Override
    public String getBaseGraph() {
        return "http://rdf.insee.fr/graphes/";
    }
}
