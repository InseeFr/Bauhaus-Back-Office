SELECT ?graph ?uri ?predicate ?value ?needHTML ?lang
WHERE {
  {
    GRAPH ?graph {
      ?subject ?link ?uri .
      ?uri rdf:type dcmitype:Text .
      ?uri rdf:value ?value .
      FILTER(STRSTARTS(STR(?graph), "http://rdf.insee.fr/graphes/qualite/rapport"))
      FILTER(
        STRENDS(LCASE(STR(?uri)), "texte") ||
        STRENDS(LCASE(STR(?uri)), "text")
      )
    }
    BIND(rdf:value AS ?predicate)
    BIND(true AS ?needHTML)
    BIND("" AS ?lang)
  }
  UNION {
    GRAPH ?graph {
      ?uri ?predicate ?value .
      FILTER(STRSTARTS(STR(?graph), "http://rdf.insee.fr/graphes/qualite/rapport"))
    }
    GRAPH <http://rdf.insee.fr/graphes/qualite/simsv2fr> {
      ?predicate rdfs:range xsd:string .
    }
    BIND(false AS ?needHTML)
    BIND(LANG(?value) AS ?lang)
  }
}
ORDER BY ?graph ?uri
LIMIT ${batchSize?c}
OFFSET ${offset?c}