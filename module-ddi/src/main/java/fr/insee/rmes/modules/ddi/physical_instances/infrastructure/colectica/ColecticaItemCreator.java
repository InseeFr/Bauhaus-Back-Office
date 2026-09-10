package fr.insee.rmes.modules.ddi.physical_instances.infrastructure.colectica;

import fr.insee.rmes.colectica.client.ColecticaClient;
import fr.insee.rmes.colectica.client.dto.ColecticaCreateItemRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi3Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Category;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CategoryScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4CodeListScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4LogicalProduct;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedMissingValuesRepresentation;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4ManagedRepresentationScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4VariableScheme;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDI4toDDI3ConverterService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enregistrement unitaire d'un objet DDI4 dans Colectica : conversion en fragment DDI 3.3 puis
 * {@code createOrUpdateItems} sur un lot d'un seul item.
 */
class ColecticaItemCreator {

    private static final Logger logger = LoggerFactory.getLogger(ColecticaItemCreator.class);

    private final ColecticaClient colecticaClient;
    private final DDI4toDDI3ConverterService ddi4ToDdi3Converter;

    ColecticaItemCreator(ColecticaClient colecticaClient, DDI4toDDI3ConverterService ddi4ToDdi3Converter) {
        this.colecticaClient = colecticaClient;
        this.ddi4ToDdi3Converter = ddi4ToDdi3Converter;
    }

    void createLogicalProduct(Ddi4LogicalProduct logicalProduct) {
        create(
                "logical product",
                logicalProduct.agency(),
                logicalProduct.id(),
                ddi4ToDdi3Converter.toLogicalProductItem(logicalProduct));
    }

    void createCodeListScheme(Ddi4CodeListScheme codeListScheme) {
        create(
                "code list scheme",
                codeListScheme.agency(),
                codeListScheme.id(),
                ddi4ToDdi3Converter.toCodeListSchemeItem(codeListScheme));
    }

    void createCategoryScheme(Ddi4CategoryScheme categoryScheme) {
        create(
                "category scheme",
                categoryScheme.agency(),
                categoryScheme.id(),
                ddi4ToDdi3Converter.toCategorySchemeItem(categoryScheme));
    }

    void createVariableScheme(Ddi4VariableScheme variableScheme) {
        create(
                "variable scheme",
                variableScheme.agency(),
                variableScheme.id(),
                ddi4ToDdi3Converter.toVariableSchemeItem(variableScheme));
    }

    void createManagedRepresentationScheme(Ddi4ManagedRepresentationScheme managedRepresentationScheme) {
        create(
                "managed representation scheme",
                managedRepresentationScheme.agency(),
                managedRepresentationScheme.id(),
                ddi4ToDdi3Converter.toManagedRepresentationSchemeItem(managedRepresentationScheme));
    }

    void createManagedMissingValuesRepresentation(
            Ddi4ManagedMissingValuesRepresentation managedMissingValuesRepresentation) {
        create(
                "managed missing values representation",
                managedMissingValuesRepresentation.agency(),
                managedMissingValuesRepresentation.id(),
                ddi4ToDdi3Converter.toManagedMissingValuesRepresentationItem(managedMissingValuesRepresentation));
    }

    void createCodeList(Ddi4CodeList codeList) {
        create("code list", codeList.agency(), codeList.id(), ddi4ToDdi3Converter.toCodeListItem(codeList));
    }

    void createCategory(Ddi4Category category) {
        create("category", category.agency(), category.id(), ddi4ToDdi3Converter.toCategoryItem(category));
    }

    private void create(String what, String agency, String id, Ddi3Response.Ddi3Item item) {
        logger.info("Creating {} in Colectica: {}/{}", what, agency, id);
        colecticaClient.createOrUpdateItems(
                new ColecticaCreateItemRequest(List.of(ColecticaItems.toColecticaItem(item))));
    }
}
