select ?changeNoteURI where {
?concept skos:changeNote ?changeNoteURI .
FILTER(REGEX(STR(?concept),'/concepts/definition/${CONCEPT_ID}')) .
?changeNoteURI dcterms:language '${LANG}'^^xsd:language .
?changeNoteURI insee:conceptVersion '${CONCEPT_VERSION}'^^xsd:int}