SELECT DISTINCT ?iri ?id ?lastCodeUriSegment
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?iri skos:notation '${NOTATION}' ;
    skos:notation ?id .
    OPTIONAL {
        ?iri insee:lastCodeUriSegment ?lastCodeUriSegment .
    }
}