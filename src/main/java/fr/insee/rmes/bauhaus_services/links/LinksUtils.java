package fr.insee.rmes.bauhaus_services.links;

import java.util.List;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.SKOS;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.model.links.Link;

public class LinksUtils {

	//TODO generalize or move in concepts
	
	public void createRdfLinks(URI conceptURI, List<Link> links, Model model) {

		// Create links
		links.forEach(link -> {

			if (link.getTypeOfLink().equals("narrower")) {
				addTripleNarrower(conceptURI, link.getIds(), model);
			}
			if (link.getTypeOfLink().equals("broader")) {
				addTripleBroader(conceptURI, link.getIds(), model);
			}
			if (link.getTypeOfLink().equals("references")) {
				addTripleReferences(conceptURI, link.getIds(), model);
			}
			if (link.getTypeOfLink().equals("succeed")) {
				addTripleSucceed(conceptURI, link.getIds(), model);
			}
			if (link.getTypeOfLink().equals("related")) {
				addTripleRelated(conceptURI, link.getIds(), model);
			}
		});

	}

	private void addTripleBroader(URI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, SKOS.NARROWER, RdfUtils.conceptIRI(conceptIDToLink), RdfUtils.conceptGraph());
			model.add(RdfUtils.conceptIRI(conceptIDToLink), SKOS.BROADER, conceptURI, RdfUtils.conceptGraph());
		});
	}

	private void addTripleNarrower(URI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, SKOS.BROADER, RdfUtils.conceptIRI(conceptIDToLink), RdfUtils.conceptGraph());
			model.add(RdfUtils.conceptIRI(conceptIDToLink), SKOS.NARROWER, conceptURI, RdfUtils.conceptGraph());
		});
	}

	private void addTripleReferences(URI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, DCTERMS.REFERENCES, RdfUtils.conceptIRI(conceptIDToLink),
					RdfUtils.conceptGraph());
		});
	}

	private void addTripleSucceed(URI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, DCTERMS.REPLACES, RdfUtils.conceptIRI(conceptIDToLink),
					RdfUtils.conceptGraph());
		});
	}

	private void addTripleRelated(URI conceptURI, List<String> conceptsIDToLink, Model model) {
		conceptsIDToLink.forEach(conceptIDToLink -> {
			model.add(conceptURI, SKOS.RELATED, RdfUtils.conceptIRI(conceptIDToLink), RdfUtils.conceptGraph());
		});
	}
	
}
