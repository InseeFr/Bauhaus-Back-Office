SELECT
?definitionLg1 ?definitionLg2
?scopeNoteLg1 ?scopeNoteLg2
?coreContentNoteLg1 ?coreContentNoteLg2
?additionalContentNoteLg1 ?additionalContentNoteLg2
?exclusionNoteLg1 ?exclusionNoteLg2
?changeNoteLg1 ?changeNoteLg2 ?changeNoteDate

WHERE {
    ?item skos:inScheme ?classification .
    FILTER(REGEX(STR(?classification),'/codes/${CLASSIFICATION_ID}/'))
    FILTER(STRENDS(STR(?item),'/${ITEM_ID}'))

    OPTIONAL {
        ?item skos:definition ?defLg1 .
        ?defLg1 dcterms:language '${LG1}'^^xsd:language .
        ?defLg1 evoc:noteLiteral ?definitionLg1 .
        ?defLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:definition ?defLg2 .
        ?defLg2 dcterms:language '${LG2}'^^xsd:language .
        ?defLg2 evoc:noteLiteral ?definitionLg2 .
        ?defLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:scopeNote ?scopeLg1 .
        ?scopeLg1 dcterms:language '${LG1}'^^xsd:language .
        ?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 .
        ?scopeLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:scopeNote ?scopeLg2 .
        ?scopeLg2 dcterms:language '${LG2}'^^xsd:language .
        ?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 .
        ?scopeLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:coreContentNote ?coreContentLg1 .
        ?coreContentLg1 dcterms:language '${LG1}'^^xsd:language .
        ?coreContentLg1 evoc:noteLiteral ?coreContentNoteLg1 .
        ?coreContentLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:coreContentNote ?coreContentLg2 .
        ?coreContentLg2 dcterms:language '${LG2}'^^xsd:language .
        ?coreContentLg2 evoc:noteLiteral ?coreContentNoteLg2 .
        ?coreContentLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:additionalContentNote ?additionalContentLg1 .
        ?additionalContentLg1 dcterms:language '${LG1}'^^xsd:language .
        ?additionalContentLg1 evoc:noteLiteral ?additionalContentNoteLg1 .
        ?additionalContentLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:additionalContentNote ?additionalContentLg2 .
        ?additionalContentLg2 dcterms:language '${LG2}'^^xsd:language .
        ?additionalContentLg2 evoc:noteLiteral ?additionalContentNoteLg2 .
        ?additionalContentLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:exclusionNote ?exclusionLg1 .
        ?exclusionLg1 dcterms:language '${LG1}'^^xsd:language .
        ?exclusionLg1 evoc:noteLiteral ?exclusionNoteLg1 .
        ?exclusionLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:exclusionNote ?exclusionLg2 .
        ?exclusionLg2 dcterms:language '${LG2}'^^xsd:language .
        ?exclusionLg2 evoc:noteLiteral ?exclusionNoteLg2 .
        ?exclusionLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:changeNote ?noteChangeLg1 .
        ?noteChangeLg1 dcterms:issued ?changeNoteDate .
        ?noteChangeLg1 dcterms:language '${LG1}'^^xsd:language .
        ?noteChangeLg1 evoc:noteLiteral ?changeNoteLg1 .
        ?noteChangeLg1 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int
    } .

    OPTIONAL {
        ?item skos:changeNote ?noteChangeLg2 .
        ?noteChangeLg2 dcterms:language '${LG2}'^^xsd:language .
        ?noteChangeLg2 evoc:noteLiteral ?changeNoteLg2 .
        ?noteChangeLg2 insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int
    } .
}