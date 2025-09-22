package fr.insee.rmes.bauhaus_services.classifications;

import fr.insee.rmes.bauhaus_services.rdf_utils.RdfUtils;
import fr.insee.rmes.bauhaus_services.rdf_utils.RepositoryGestion;
import fr.insee.rmes.domain.exceptions.RmesException;
import fr.insee.rmes.graphdb.ontologies.EVOC;
import fr.insee.rmes.graphdb.ontologies.XKOS;
import fr.insee.rmes.utils.XhtmlToMarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.stereotype.Service;

@Service
public class ClassificationNoteServiceImpl implements ClassificationNoteService {
    private RepositoryGestion repositoryGestion;

    public ClassificationNoteServiceImpl(RepositoryGestion repositoryGestion) {
        this.repositoryGestion = repositoryGestion;
    }

    public void addNotes(Resource graph, String iri, String value, Model model) throws RmesException {
        if (StringUtils.isNotEmpty(iri)) {
            IRI noteIri = RdfUtils.createIRI(iri);
            repositoryGestion.deleteTripletByPredicate(noteIri, EVOC.NOTE_LITERAL, graph, null);
            repositoryGestion.deleteTripletByPredicate(noteIri, XKOS.PLAIN_TEXT, graph, null);
            repositoryGestion.deleteTripletByPredicate(noteIri, RDF.VALUE, graph, null);
            if (StringUtils.isNotEmpty(value)) {
                String html = XhtmlToMarkdownUtils.markdownToXhtml(value);
                String raw = html.replaceAll("<[^>]*>", "");
                model.add(noteIri, EVOC.NOTE_LITERAL, RdfUtils.setLiteralString("<div xmlns=\"http://www.w3.org/1999/xhtml\">" + html.trim() + "</div>"), graph);
                model.add(noteIri, XKOS.PLAIN_TEXT, RdfUtils.setLiteralString(raw), graph);
                model.add(noteIri, RDF.VALUE, RdfUtils.setLiteralString(value), graph);
            }
        }
    }
}
