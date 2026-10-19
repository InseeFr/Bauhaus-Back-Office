<#-- SERIES_IRIS contient des IRI complètes (<…>), échappées par SparqlLiterals. -->
<#-- Un créateur stocké en IRI d'organisation est rendu par son adms:identifier, la forme du timbre
     de l'utilisateur ; un créateur littéral est rendu tel quel. Même règle que getSeriesWithStampQuery. -->
SELECT ?seriesIri ?creators
WHERE {
    GRAPH ${OPERATIONS_GRAPH} {
        VALUES ?seriesIri { <#list SERIES_IRIS as iri>${iri} </#list>}
        {
            ?seriesIri dc:creator ?creator .
        }
        UNION
        {
            ?parent dc:creator ?creator .
            ?parent dcterms:hasPart ?seriesIri .
        }
    }
    OPTIONAL {
        GRAPH ${ORG_INSEE_GRAPH} {
            ?creator adms:identifier ?creatorStamp .
        }
    }
    BIND(COALESCE(?creatorStamp, STR(?creator)) AS ?creators)
}
