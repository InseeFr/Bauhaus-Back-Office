package fr.insee.rmes.bauhaus_services.structures;

import fr.insee.rmes.exceptions.RmesException;

public interface StructureComponent {
    public String getComponentsForSearch() throws RmesException;

    public String getComponents() throws RmesException;

    public String getComponent(String id) throws RmesException;

    public String updateComponent(String componentId, String body) throws RmesException;

    public String createComponent(String body) throws RmesException;

    public void deleteComponent(String id) throws RmesException;


}
