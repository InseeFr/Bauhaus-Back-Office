package fr.insee.rmes.bauhaus_services.structures;

import fr.insee.rmes.exceptions.RmesException;

public interface StructureComponent {
    String getComponentsForSearch() throws RmesException;

    String getComponents() throws RmesException;

    String getComponent(String id) throws RmesException;

    String updateComponent(String componentId, String body) throws RmesException;

    String createComponent(String body) throws RmesException;

    void deleteComponent(String id) throws RmesException;

    String publishComponent(String id) throws RmesException;

    String getAttributes() throws RmesException;
}
