package fr.insee.rmes.config;

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
    public String getOrgInseeGraph() {
        return "http://rdf.insee.fr/graphes/organisations/insee";
    }

    //TODO
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
}
