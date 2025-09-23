package fr.insee.rmes.bauhaus_services.concepts.concepts;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.model.links.Link;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import java.util.List;

public class LinksUtils {

	public void createRdfLinks(IRI conceptURI, List<Link> links, Model model) {

		// Create links
		links.forEach(link -> {
			if (link.getTypeOfLink().equals("narrower")) {
				addTripleNarrower(conceptURI, link.getIds(), model);
			}
			else if (link.getTypeOfLink().equals("broader")) {
				addTripleBroader(conceptURI, link.getIds(), model);
			}
			else if (link.getTypeOfLink().equals("references")) {
				addTripleReferences(conceptURI, link.getIds(), model);
			}
			else if (link.getTypeOfLink().equals("succeed")) {
				addTripleSucceed(conceptURI, link.getIds(), model);
			}
			else if (link.getTypeOfLink().equals("related")) {
				addTripleRelated(conceptURI, link.getIds(), model);
			}
			else if (link.getTypeOfLink().equals("closeMatch")){
				addTripleCloseMatch(conceptURI, link.getUrn(), model);
			} else if (link.getTypeOfLink().equals("succeededBy")){
				addTripleReplacedByMatch(conceptURI, link.getIds(), model);
			}
			
		});

	}

	private void addTripleReplacedByMatch(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, DCTERMS.IS_REPLACED_BY, RdfUtils.conceptIRI(conceptIDToLink), RdfUtils.conceptGraph());
			model.add(RdfUtils.conceptIRI(conceptIDToLink), DCTERMS.REPLACES, conceptURI, RdfUtils.conceptGraph());
		});
	}

	private void addTripleSucceed(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, DCTERMS.REPLACES, RdfUtils.conceptIRI(conceptIDToLink),
					RdfUtils.conceptGraph());
			model.add(RdfUtils.conceptIRI(conceptIDToLink), DCTERMS.IS_REPLACED_BY, conceptURI, RdfUtils.conceptGraph());
		});
	}

	private void addTripleCloseMatch(IRI conceptURI, List<String> urn, Model model) {
		urn.forEach(urnToAdd -> model.add(conceptURI, SKOS.CLOSE_MATCH, RdfUtils.toURI(urnToAdd), RdfUtils.conceptGraph()));
	}

	private void addTripleBroader(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, SKOS.BROADER, RdfUtils.conceptIRI(conceptIDToLink), RdfUtils.conceptGraph());
			model.add(RdfUtils.conceptIRI(conceptIDToLink), SKOS.NARROWER, conceptURI, RdfUtils.conceptGraph());
		});
	}

	private void addTripleNarrower(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, SKOS.NARROWER, RdfUtils.conceptIRI(conceptIDToLink), RdfUtils.conceptGraph());
			model.add(RdfUtils.conceptIRI(conceptIDToLink), SKOS.BROADER, conceptURI, RdfUtils.conceptGraph());
		});
	}

	private void addTripleReferences(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> 
			model.add(conceptURI, DCTERMS.REFERENCES, RdfUtils.conceptIRI(conceptIDToLink),
					RdfUtils.conceptGraph()));
	}

	private void addTripleRelated(IRI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, SKOS.RELATED, RdfUtils.conceptIRI(conceptIDToLink), RdfUtils.conceptGraph());
			model.add(RdfUtils.conceptIRI(conceptIDToLink), SKOS.RELATED, conceptURI, RdfUtils.conceptGraph());
		});
	}
	
}
