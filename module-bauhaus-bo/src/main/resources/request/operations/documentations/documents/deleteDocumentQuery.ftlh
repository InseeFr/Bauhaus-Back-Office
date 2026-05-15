WITH <${DOCUMENTS_GRAPH}>

DELETE  {

	 ?uri rdf:type foaf:Document .
	 ?uri <http://schema.org/url> ?url .
 	 ?uri rdfs:label ?label .
	 ?uri rdfs:comment ?description .
	 ?uri pav:lastRefreshedOn ?updatedDate .
	 ?uri dc:language ?lang .
	}
 WHERE {

	 ?uri rdf:type foaf:Document .
	 ?uri <http://schema.org/url> ?url .
 	OPTIONAL { ?uri rdfs:label ?label }
	OPTIONAL {  ?uri rdfs:comment ?description }
	OPTIONAL {  ?uri pav:lastRefreshedOn ?updatedDate }
	OPTIONAL {  ?uri dc:language ?lang }
	 FILTER(?uri = <${uri}>)
	  }
	;

# Défense en profondeur : si une cellule rdf:List pointait encore vers ce document
# (cas dégénéré non bloqué par checkDocumentReference), on splice la liste pour ne
# pas laisser de cellule pendante ni de prédécesseur orphelin. Les cellules vivent
# dans le graphe SIMS, pas dans DOCUMENTS_GRAPH ; on parcourt donc tous les graphes.

DELETE { GRAPH ?g { ?prev rdf:rest ?cell . } }
INSERT { GRAPH ?g { ?prev rdf:rest ?cellRest . } }
WHERE {
	GRAPH ?g {
		?cell rdf:first <${uri}> .
		?cell rdf:rest ?cellRest .
		?prev rdf:rest ?cell .
	}
}
	;

DELETE { GRAPH ?g { ?text insee:additionalMaterial ?cell . } }
INSERT { GRAPH ?g { ?text insee:additionalMaterial ?cellRest . } }
WHERE {
	GRAPH ?g {
		?cell rdf:first <${uri}> .
		?cell rdf:rest ?cellRest .
		?text insee:additionalMaterial ?cell .
		FILTER(?cellRest != rdf:nil)
	}
}
	;

DELETE { GRAPH ?g { ?text insee:additionalMaterial ?cell . } }
WHERE {
	GRAPH ?g {
		?cell rdf:first <${uri}> .
		?cell rdf:rest rdf:nil .
		?text insee:additionalMaterial ?cell .
	}
}
	;

DELETE { GRAPH ?g { ?cell rdf:first ?docFirst . ?cell rdf:rest ?cellRest . } }
WHERE {
	GRAPH ?g {
		?cell rdf:first <${uri}> .
		?cell rdf:first ?docFirst .
		OPTIONAL { ?cell rdf:rest ?cellRest . }
	}
}
