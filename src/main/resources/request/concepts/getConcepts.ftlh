SELECT DISTINCT ?id ?label ?altLabel
WHERE {
    GRAPH <${CONCEPTS_GRAPH}> {
        ?concept skos:notation ?notation .
        BIND (STR(?notation) AS ?id)
        ?concept skos:prefLabel ?label .
        FILTER (lang(?label) = '${LG1}')
<<<<<<< Updated upstream
        OPTIONAL{?concept skos:altLabel ?altLabelLg1 .
        FILTER (lang(?altLabelLg1) = '${LG1}')}
    }
}
GROUP BY ?id ?label
=======
        OPTIONAL{
            ?concept skos:altLabel ?altLabel .
            FILTER (lang(?altLabel) = '${LG1}')
        }
    }
}
>>>>>>> Stashed changes
ORDER BY ?label