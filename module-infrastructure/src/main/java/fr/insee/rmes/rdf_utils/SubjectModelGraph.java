package fr.insee.rmes.rdf_utils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public record SubjectModelGraph(IRI subject, Model model, Resource graph) {}