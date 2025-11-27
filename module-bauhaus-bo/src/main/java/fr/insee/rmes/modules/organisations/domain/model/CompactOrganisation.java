package fr.insee.rmes.modules.organisations.domain.model;

import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import org.eclipse.rdf4j.model.IRI;

public record CompactOrganisation(IRI iri, String identifier, LocalisedLabel label) {
}
