SELECT
?definitionLg1Uri ?definitionLg1
?definitionLg2Uri ?definitionLg2
?scopeNoteLg1Uri ?scopeNoteLg1
?scopeNoteLg2Uri ?scopeNoteLg2
?coreContentNoteLg1Uri ?coreContentNoteLg1
?coreContentNoteLg2Uri ?coreContentNoteLg2
?additionalContentNoteLg1Uri ?additionalContentNoteLg1
?additionalContentNoteLg2Uri ?additionalContentNoteLg2
?exclusionNoteLg1Uri ?exclusionNoteLg1
?exclusionNoteLg2Uri ?exclusionNoteLg2
?changeNoteLg1Uri ?changeNoteLg1
?changeNoteLg2Uri ?changeNoteLg2
?changeNoteDate

WHERE {
    ?item skos:inScheme ?classification .
    FILTER(REGEX(STR(?classification),'/codes/${CLASSIFICATION_ID}/'))
    FILTER(STRENDS(STR(?item),'/${ITEM_ID}'))

    OPTIONAL {
        ?item skos:definition ?definitionLg1Uri .
        ?definitionLg1Uri dcterms:language '${LG1}'^^xsd:language .
        ?definitionLg1Uri evoc:noteLiteral ?definitionLg1 .
        ?definitionLg1Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:definition ?definitionLg2Uri .
        ?definitionLg2Uri dcterms:language '${LG2}'^^xsd:language .
        ?definitionLg2Uri evoc:noteLiteral ?definitionLg2 .
        ?definitionLg2Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:scopeNote ?scopeNoteLg1Uri .
        ?scopeNoteLg1Uri dcterms:language '${LG1}'^^xsd:language .
        ?scopeNoteLg1Uri evoc:noteLiteral ?scopeNoteLg1 .
        ?scopeNoteLg1Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:scopeNote ?scopeNoteLg2Uri .
        ?scopeNoteLg2Uri dcterms:language '${LG2}'^^xsd:language .
        ?scopeNoteLg2Uri evoc:noteLiteral ?scopeNoteLg2 .
        ?scopeNoteLg2Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:coreContentNote ?coreContentNoteLg1Uri .
        ?coreContentNoteLg1Uri dcterms:language '${LG1}'^^xsd:language .
        ?coreContentNoteLg1Uri evoc:noteLiteral ?coreContentNoteLg1 .
        ?coreContentNoteLg1Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:coreContentNote ?coreContentNoteLg2Uri .
        ?coreContentNoteLg2Uri dcterms:language '${LG2}'^^xsd:language .
        ?coreContentNoteLg2Uri evoc:noteLiteral ?coreContentNoteLg2 .
        ?coreContentNoteLg2Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:additionalContentNote ?additionalContentNoteLg1Uri .
        ?additionalContentNoteLg1Uri dcterms:language '${LG1}'^^xsd:language .
        ?additionalContentNoteLg1Uri evoc:noteLiteral ?additionalContentNoteLg1 .
        ?additionalContentNoteLg1Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:additionalContentNote ?additionalContentNoteLg2Uri .
        ?additionalContentNoteLg2Uri dcterms:language '${LG2}'^^xsd:language .
        ?additionalContentNoteLg2Uri evoc:noteLiteral ?additionalContentNoteLg2 .
        ?additionalContentNoteLg2Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:exclusionNote ?exclusionNoteLg1Uri .
        ?exclusionNoteLg1Uri dcterms:language '${LG1}'^^xsd:language .
        ?exclusionNoteLg1Uri evoc:noteLiteral ?exclusionNoteLg1 .
        ?exclusionNoteLg1Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item xkos:exclusionNote ?exclusionNoteLg2Uri .
        ?exclusionNoteLg2Uri dcterms:language '${LG2}'^^xsd:language .
        ?exclusionNoteLg2Uri evoc:noteLiteral ?exclusionNoteLg2 .
        ?exclusionNoteLg2Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int .
    } .

    OPTIONAL {
        ?item skos:changeNote ?changeNoteLg1Uri .
        ?changeNoteLg1Uri dcterms:issued ?changeNoteDate .
        ?changeNoteLg1Uri dcterms:language '${LG1}'^^xsd:language .
        ?changeNoteLg1Uri evoc:noteLiteral ?changeNoteLg1 .
        ?changeNoteLg1Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int
    } .

    OPTIONAL {
        ?item skos:changeNote ?changeNoteLg2Uri .
        ?changeNoteLg2Uri dcterms:language '${LG2}'^^xsd:language .
        ?changeNoteLg2Uri evoc:noteLiteral ?changeNoteLg2 .
        ?changeNoteLg2Uri insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int
    } .
}