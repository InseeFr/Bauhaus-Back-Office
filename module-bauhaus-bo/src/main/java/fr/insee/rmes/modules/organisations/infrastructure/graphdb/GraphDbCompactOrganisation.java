package fr.insee.rmes.modules.organisations.infrastructure.graphdb;

import fr.insee.rmes.modules.commons.domain.model.Lang;
import fr.insee.rmes.modules.commons.domain.model.LocalisedLabel;
import fr.insee.rmes.modules.organisations.domain.model.CompactOrganisation;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public record GraphDbCompactOrganisation(String iri, String identifier, String label, String label_lg) {
    CompactOrganisation toDomain(){
        IRI iri = SimpleValueFactory.getInstance().createIRI(this.iri);
        return new CompactOrganisation(iri, identifier, new LocalisedLabel(label, Lang.valueOf(label_lg.toUpperCase())));
    }
}
