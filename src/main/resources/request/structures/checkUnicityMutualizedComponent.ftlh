SELECT DISTINCT ?component
FROM<${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${CODES_LISTS_GRAPH}>
FROM <${CONCEPT_GRAPH}>
WHERE {
    ?component qb:concept <${CONCEPT_URI}> ;
    		   qb:codeList <${CODE_LIST_URI}> .
    FILTER NOT EXISTS {
    	?component dcterms:identifier '${COMPONENT_ID}' .
	}
}