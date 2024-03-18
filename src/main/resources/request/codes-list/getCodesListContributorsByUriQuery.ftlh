SELECT ?contributors
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    BIND(STRAFTER(STR(?o),'http://bauhaus/codes/') AS ?lastCodeUriSegment) .
    BIND(STRBEFORE(STRAFTER(STR('${URI_CODESLIST?no_esc}'),'http://bauhaus/codes/'),'>') AS ?lastCodeUriSegmentWithId) .
?s ?p ?o .
?o skos:notation ?lastCodeUriSegmentWithId .
?o dc:contributor ?contributors
}



