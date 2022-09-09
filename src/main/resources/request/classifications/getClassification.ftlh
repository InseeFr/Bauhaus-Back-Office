SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2 ?seriesLg1 ?seriesLg2 ?idSeries
?afterLg1 ?afterLg2 ?idAfter ?beforeLg1 ?beforeLg2 ?idBefore ?variantLg1 ?variantLg2 ?idVariant 
?issued ?valid ?lastRefreshedOn ?additionalMaterial ?rights ?creator ?contributor 
?disseminationStatus ?legalMaterial ?homepage 
?scopeNoteLg1 ?scopeNoteLg2 ?changeNoteLg1 ?changeNoteLg2 ?descriptionLg1 ?descriptionLg2
?changeNoteUriLg1 ?changeNoteUriLg2 ?scopeNoteUriLg1 ?scopeNoteUriLg2
WHERE {
    ?classification rdf:type skos:ConceptScheme .

    FILTER(REGEX(STR(?classification),'/codes/${ID}/')) .

    BIND(STRBEFORE(STRAFTER(STR(?classification),'/codes/'), '/') AS ?id)
    ?classification skos:prefLabel ?prefLabelLg1 .
    FILTER (lang(?prefLabelLg1) = '${LG1}') .

    OPTIONAL {
        ?classification skos:prefLabel ?prefLabelLg2 .
        FILTER (lang(?prefLabelLg2) = '${LG2}')
    } .

    {
        OPTIONAL {
            SELECT (group_concat(?altLg1;separator=' || ') as ?altLabelLg1)
            WHERE {
                ?classification rdf:type skos:ConceptScheme .
                ?classification skos:altLabel ?altLg1 .
                FILTER (lang(?altLg1) = '${LG1}')  .
                FILTER(REGEX(STR(?classification),'/codes/${ID}/')) .
            }
        }
    }

    {
        OPTIONAL {
            SELECT (group_concat(?altLg2;separator=' || ') as ?altLabelLg2)
            WHERE {
                ?classification rdf:type skos:ConceptScheme .
                ?classification skos:altLabel ?altLg2 .
                FILTER (lang(?altLg2) = '${LG2}')  .
                FILTER(REGEX(STR(?classification),'/codes/${ID}/')) .
            }
        }
    }

    OPTIONAL {
        ?classification skos:scopeNote ?scopeNoteUriLg1 .
        ?scopeNoteUriLg1 dcterms:language '${LG1}'^^xsd:language .
        ?scopeNoteUriLg1 evoc:noteLiteral ?scopeNoteLg1 .
    } .

    OPTIONAL {
        ?classification skos:scopeNote ?scopeNoteUriLg2 .
        ?scopeNoteUriLg2 dcterms:language '${LG2}'^^xsd:language .
        ?scopeNoteUriLg2 evoc:noteLiteral ?scopeNoteLg2 .
    } .

    OPTIONAL {
        ?classification skos:changeNote ?changeNoteUriLg1 .
        ?changeNoteUriLg1 dcterms:language '${LG1}'^^xsd:language .
        ?changeNoteUriLg1 evoc:noteLiteral ?changeNoteLg1 .
    } .

    OPTIONAL {
        ?classification skos:changeNote ?changeNoteUriLg2 .
        ?changeNoteUriLg2 dcterms:language '${LG2}'^^xsd:language .
        ?changeNoteUriLg2 evoc:noteLiteral ?changeNoteLg2 .
    } .

    OPTIONAL {
        ?classification dc:description ?descriptionLg1  .
        FILTER (lang(?descriptionLg1) = '${LG1}')
    }

    OPTIONAL {
        ?classification dc:description ?descriptionLg2  .
        FILTER (lang(?descriptionLg2) = '${LG2}')
    }

    OPTIONAL {
        ?classification xkos:belongsTo ?seriesURI .
        ?seriesURI skos:prefLabel ?seriesLg1 .
        FILTER (lang(?seriesLg1) = '${LG1}')  .
        BIND(STRAFTER(STR(?seriesURI),'/codes/serieDeNomenclatures/') AS ?idSeries)
    } .

    OPTIONAL {
        ?classification xkos:belongsTo ?seriesURI .
        ?seriesURI skos:prefLabel ?seriesLg2 .
        FILTER (lang(?seriesLg2) = '${LG2}')
    }  .

    OPTIONAL {
        ?classification xkos:after ?afterURI .
        ?afterURI skos:prefLabel ?afterLg1 .
        FILTER (lang(?afterLg1) = '${LG1}')  .
        BIND(STRBEFORE(STRAFTER(STR(?afterURI),'/codes/'), '/') AS ?idAfter)
    } .

    OPTIONAL {
        ?classification xkos:after ?afterURI .
        ?afterURI skos:prefLabel ?afterLg2 .
        FILTER (lang(?afterLg2) = '${LG2}')
    }  .

    OPTIONAL {
        ?classification xkos:before ?beforeURI .
        ?beforeURI skos:prefLabel ?beforeLg1 .
        FILTER (lang(?beforeLg1) = '${LG1}')  .
        BIND(STRBEFORE(STRAFTER(STR(?beforeURI),'/codes/'), '/') AS ?idBefore)
    } .

    OPTIONAL {
        ?classification xkos:before ?beforeURI .
        ?beforeURI skos:prefLabel ?beforeLg2 .
        FILTER (lang(?beforeLg2) = '${LG2}')
    }  .

    OPTIONAL {
        ?classification xkos:variant ?variantURI .
        ?variantURI skos:prefLabel ?variantLg1 .
        FILTER (lang(?variantLg1) = '${LG1}')  .
        BIND(STRBEFORE(STRAFTER(STR(?variantURI),'/codes/'), '/') AS ?idVariant)
    } .

    OPTIONAL {
        ?classification xkos:variant ?variantURI .
        ?variantURI skos:prefLabel ?variantLg2 .
        FILTER (lang(?variantLg2) = '${LG2}')
    }  .

    OPTIONAL {?classification dcterms:issued ?issued} .
    OPTIONAL {?classification dcterms:valid ?valid} .
    OPTIONAL {?classification pav:lastRefreshedOn ?lastRefreshedOn} .
    OPTIONAL {?classification insee:additionalMaterial ?additionalMaterial} .
    OPTIONAL {?classification dc:rights ?rights} .
    OPTIONAL {?classification dc:contributor ?contributor} .
    OPTIONAL {?classification dc:creator ?creator} .
    OPTIONAL {?classification insee:disseminationStatus ?disseminationStatus} .
    OPTIONAL {?classification insee:legalMaterial ?legalMaterial} .
    OPTIONAL {?classification foaf:homepage ?homepage} .
}