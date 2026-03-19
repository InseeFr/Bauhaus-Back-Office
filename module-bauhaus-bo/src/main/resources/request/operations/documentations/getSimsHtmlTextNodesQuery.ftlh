SELECT ?graph ?uri ?value
WHERE {
  GRAPH ?graph {
    ?uri rdf:type dcmitype:Text .
    ?uri rdf:value ?value .
    FILTER(CONTAINS(STR(?graph), "qualite/rapport"))
    FILTER(
      STRENDS(LCASE(STR(?uri)), "texte") ||
      STRENDS(LCASE(STR(?uri)), "text")
    )
  }
}
ORDER BY ?graph ?uri