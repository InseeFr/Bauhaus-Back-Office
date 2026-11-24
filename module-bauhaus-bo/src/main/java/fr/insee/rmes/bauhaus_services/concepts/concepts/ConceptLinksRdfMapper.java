package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.BauhausIriFactory;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfTriples;
import fr.insee.rmes.model.links.Link;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

public class ConceptLinksRdfMapper {

    private final BauhausIriFactory iriFactory;

    public ConceptLinksRdfMapper(BauhausIriFactory iriFactory) {
        this.iriFactory = iriFactory;
    }

    public void createRdfLinks(IRI conceptURI, List<Link> links, Model model) {

        // Create links
        if (links == null) {
            return;
        }
        links.forEach(link -> {
            if (link.getTypeOfLink().equals("narrower")) {
                addTripleNarrower(conceptURI, link.getIds(), model);
            } else if (link.getTypeOfLink().equals("broader")) {
                addTripleBroader(conceptURI, link.getIds(), model);
            } else if (link.getTypeOfLink().equals("references")) {
                addTripleReferences(conceptURI, link.getIds(), model);
            } else if (link.getTypeOfLink().equals("succeed")) {
                addTripleSucceed(conceptURI, link.getIds(), model);
            } else if (link.getTypeOfLink().equals("related")) {
                addTripleRelated(conceptURI, link.getIds(), model);
            } else if (link.getTypeOfLink().equals("closeMatch")) {
                addTripleCloseMatch(conceptURI, link.getUrn(), model);
            } else if (link.getTypeOfLink().equals("succeededBy")) {
                addTripleReplacedByMatch(conceptURI, link.getIds(), model);
            }
        });
    }

    private void addTripleReplacedByMatch(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
        conceptsIDToLink.forEach(conceptIDToLink -> {
            model.add(
                    conceptURI, DCTERMS.IS_REPLACED_BY, iriFactory.concept(conceptIDToLink), iriFactory.conceptGraph());
            model.add(iriFactory.concept(conceptIDToLink), DCTERMS.REPLACES, conceptURI, iriFactory.conceptGraph());
        });
    }

    private void addTripleSucceed(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
        conceptsIDToLink.forEach(conceptIDToLink -> {
            model.add(conceptURI, DCTERMS.REPLACES, iriFactory.concept(conceptIDToLink), iriFactory.conceptGraph());
            model.add(
                    iriFactory.concept(conceptIDToLink), DCTERMS.IS_REPLACED_BY, conceptURI, iriFactory.conceptGraph());
        });
    }

    private void addTripleCloseMatch(IRI conceptURI, List<String> urn, Model model) {
        urn.forEach(urnToAdd ->
                model.add(conceptURI, SKOS.CLOSE_MATCH, RdfTriples.iri(urnToAdd), iriFactory.conceptGraph()));
    }

    private void addTripleBroader(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
        conceptsIDToLink.forEach(conceptIDToLink -> {
            model.add(conceptURI, SKOS.BROADER, iriFactory.concept(conceptIDToLink), iriFactory.conceptGraph());
            model.add(iriFactory.concept(conceptIDToLink), SKOS.NARROWER, conceptURI, iriFactory.conceptGraph());
        });
    }

    private void addTripleNarrower(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
        conceptsIDToLink.forEach(conceptIDToLink -> {
            model.add(conceptURI, SKOS.NARROWER, iriFactory.concept(conceptIDToLink), iriFactory.conceptGraph());
            model.add(iriFactory.concept(conceptIDToLink), SKOS.BROADER, conceptURI, iriFactory.conceptGraph());
        });
    }

    private void addTripleReferences(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
        conceptsIDToLink.forEach(conceptIDToLink -> model.add(
                conceptURI, DCTERMS.REFERENCES, iriFactory.concept(conceptIDToLink), iriFactory.conceptGraph()));
    }

    private void addTripleRelated(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
        conceptsIDToLink.forEach(conceptIDToLink -> {
            model.add(conceptURI, SKOS.RELATED, iriFactory.concept(conceptIDToLink), iriFactory.conceptGraph());
            model.add(iriFactory.concept(conceptIDToLink), SKOS.RELATED, conceptURI, iriFactory.conceptGraph());
        });
    }
}
