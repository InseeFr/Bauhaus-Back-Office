<#-- Un autre document ou lien que URI porte-t-il le libellé LABEL ? -->
ASK {
	GRAPH ${DOCUMENTS_GRAPH} {
		?uri rdf:type foaf:Document ;
			rdfs:label ${LABEL} .
		FILTER(?uri != ${URI})
	}
}
