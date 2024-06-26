SELECT DISTINCT ?component ?id ?identifiant ?labelLg1 ?labelLg2 ?type ?concept ?codeList ?range ?descriptionLg1 ?descriptionLg2 ?validationState
?created ?modified ?creator ?contributor ?disseminationStatus ?minLength ?maxLength ?minInclusive ?maxInclusive ?pattern ?fullCodeListValue
?attributeIRI ?valueIri ?altLabelLg1 ?altLabelLg2
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${CODES_LISTS_GRAPH}>
WHERE {
    ?component dcterms:identifier '${ID}' ;
               skos:notation ?identifiant ;
               rdf:type ?type ;
               rdfs:label ?labelLg1 .
    filter ( ?type = qb:DimensionProperty || ?type = qb:AttributeProperty || ?type = qb:MeasureProperty )
    OPTIONAL {
        ?component dc:creator ?creator .
    } .

    OPTIONAL {
        ?component dc:contributor ?contributor .
    } .

    OPTIONAL {
        ?component insee:disseminationStatus ?disseminationStatus .
    } .

    OPTIONAL {
        ?component insee:validationState ?validationState
    } .

    OPTIONAL {
        ?component dcterms:created ?created
    } .
    OPTIONAL {
        ?component dcterms:modified ?modified
    } .

    OPTIONAL {
        ?component qb:concept ?conceptObject
    } .

    OPTIONAL {
        ?component qb:codeList ?codeList .
        OPTIONAL {
            ?codeList prov:wasDerivedFrom ?fullCodeListValue
        }
    } .

    OPTIONAL {
        ?component rdfs:range ?range
    } .

    OPTIONAL {
        ?component ?attributeIRI ?valueIri .
        ?attributeIRI rdf:type qb:AttributeProperty
    }

    FILTER (lang(?labelLg1) = '${LG1}')
    OPTIONAL {?component rdfs:label ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .
    OPTIONAL {?component rdfs:comment ?descriptionLg1 .
    FILTER (lang(?descriptionLg1) = '${LG1}') } .
    OPTIONAL {?component rdfs:comment ?descriptionLg2 .
    FILTER (lang(?descriptionLg2) = '${LG2}') } .

    OPTIONAL {
        ?component skos:altLabel ?altLabelLg1 .
        FILTER (lang(?altLabelLg1) = '${LG1}')
        }

    OPTIONAL {
        ?component skos:altLabel ?altLabelLg2 .
        FILTER (lang(?altLabelLg2) = '${LG2}')
        }

    OPTIONAL {
        ?component xsd:minLength ?minLength .
    }
    OPTIONAL {
        ?component xsd:maxLength ?maxLength .
    }
    OPTIONAL {
        ?component xsd:minInclusive ?minInclusive .
    }
    OPTIONAL {
        ?component xsd:maxInclusive ?maxInclusive .
    }
    OPTIONAL {
        ?component xsd:pattern ?pattern .
    }
    BIND(STRAFTER(STR(?conceptObject),'concepts/definition/') AS ?concept) .
}
ORDER BY ?labelLg1