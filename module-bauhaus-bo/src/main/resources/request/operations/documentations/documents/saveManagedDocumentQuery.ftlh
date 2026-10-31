<#-- Remplace en une requête tout ce que le graphe des documents porte pour URI : rien ne reste de
     l'état précédent, et rien n'est touché hors de ce graphe. Les valeurs sont des jetons SPARQL
     complets, échappés par SparqlLiterals ; une valeur absente n'écrit pas son triplet. -->
DELETE { GRAPH ${DOCUMENTS_GRAPH} { ${URI} ?p ?o } }
WHERE { GRAPH ${DOCUMENTS_GRAPH} { ${URI} ?p ?o } }
;
INSERT DATA {
	GRAPH ${DOCUMENTS_GRAPH} {
		${URI} rdf:type foaf:Document .
<#if URL??>
		${URI} <http://schema.org/url> ${URL} .
</#if>
<#if LABEL_LG1??>
		${URI} rdfs:label ${LABEL_LG1} .
</#if>
<#if LABEL_LG2??>
		${URI} rdfs:label ${LABEL_LG2} .
</#if>
<#if DESCRIPTION_LG1??>
		${URI} rdfs:comment ${DESCRIPTION_LG1} .
</#if>
<#if DESCRIPTION_LG2??>
		${URI} rdfs:comment ${DESCRIPTION_LG2} .
</#if>
<#if LANG??>
		${URI} dc:language ${LANG} .
</#if>
<#if UPDATED_DATE??>
		${URI} pav:lastRefreshedOn ${UPDATED_DATE}^^xsd:date .
</#if>
<#if SIZE??>
		${URI} dcterms:extent ${SIZE}^^xsd:integer .
</#if>
	}
}
