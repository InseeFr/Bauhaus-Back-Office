SELECT ?correspondenceId ?associationId ?labelLg1 ?labelLg2 ?scopeNoteLg1 ?scopeNoteLg2
?sourceClassId ?sourceClassAltLabelLg1 ?sourceClassAltLabelLg2
?targetClassId ?targetClassAltLabelLg1 ?targetClassAltLabelLg2
?sourceItemId ?sourceItemLabelLg1 ?sourceItemLabelLg2
?targetItemId ?targetItemLabelLg1 ?targetItemLabelLg2
WHERE {
    ?correspondence rdf:type xkos:Correspondence .
    FILTER(STRENDS(STR(?correspondence),'/codes/${CORRESPONDENCE_ID}')) .
    BIND(STRAFTER(STR(?correspondence),'/codes/') AS ?correspondenceId)
    ?correspondence skos:prefLabel ?labelLg1 .
    FILTER (lang(?labelLg1) = '${LG1}')
    OPTIONAL {?correspondence skos:prefLabel ?labelLg2 .
    FILTER (lang(?labelLg2) = '${LG2}') } .

    ?association rdf:type xkos:ConceptAssociation .
    BIND('${ASSOCIATION_ID}' as ?associationId) .
    FILTER(STRENDS(STR(?association),'/codes/${CORRESPONDENCE_ID}/association/${ASSOCIATION_ID}')) .
    OPTIONAL {?association skos:scopeNote ?scopeLg1 .
    ?scopeLg1 dcterms:language '${LG1}'^^xsd:language .
    ?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 .
    } .
    OPTIONAL {?association skos:scopeNote ?scopeLg2 .
    ?scopeLg2 dcterms:language '${LG2}'^^xsd:language .
    ?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 .
    } .

    ?sourceClassURI rdf:type skos:ConceptScheme .
    FILTER(REGEX(STR(?sourceClassURI),'/codes/${SOURCE_CLASS_ID}/')) .
    BIND('${SOURCE_CLASS_ID}' as ?sourceClassId) .
    OPTIONAL {?sourceClassURI skos:altLabel ?sourceClassAltLabelLg1 .
    FILTER (lang(?sourceClassAltLabelLg1) = '${LG1}') } .
    OPTIONAL {?sourceClassURI skos:altLabel ?sourceClassAltLabelLg2 .
    FILTER (lang(?sourceClassAltLabelLg2) = '${LG2}') } .
    ?targetClassURI rdf:type skos:ConceptScheme .
    FILTER(REGEX(STR(?targetClassURI),'/codes/${TARGET_CLASS_ID}/')) .
    BIND('${TARGET_CLASS_ID}' as ?targetClassId) .
    OPTIONAL {?targetClassURI skos:altLabel ?targetClassAltLabelLg1 .
    FILTER (lang(?targetClassAltLabelLg1) = '${LG1}') } .
    OPTIONAL {?targetClassURI skos:altLabel ?targetClassAltLabelLg2 .
    FILTER (lang(?targetClassAltLabelLg2) = '${LG2}') } .

    ?association xkos:sourceConcept ?sourceItemURI .
    BIND('${SOURCE_ITEM_ID}' as ?sourceItemId) .
    ?sourceItemURI skos:prefLabel ?sourceItemLabelLg1 .
    FILTER (lang(?sourceItemLabelLg1) = '${LG1}')
    OPTIONAL {?sourceItemURI skos:prefLabel ?sourceItemLabelLg2 .
    FILTER (lang(?sourceItemLabelLg2) = '${LG2}')}
    ?association xkos:targetConcept ?targetItemURI .
    BIND('${TARGET_ITEM_ID}' as ?targetItemId) .
    ?targetItemURI skos:prefLabel ?targetItemLabelLg1 .
    FILTER (lang(?targetItemLabelLg1) = '${LG1}')
    OPTIONAL {?targetItemURI skos:prefLabel ?targetItemLabelLg2 .
    FILTER (lang(?targetItemLabelLg2) = '${LG2}')}
}
LIMIT 1
