SELECT ?id
FROM <${STRUCTURES_COMPONENTS_GRAPH}>
FROM <${STRUCTURES_GRAPH}>
WHERE
{
    ?structure dcterms:identifier '${ID}' .
    ?structure qb:component ?componentSpecification .
    ?componentSpecification (qb:dimension|qb:measure|qb:attribute) ?component .
    ?component insee:validationState ?isValidated .
    ?component dcterms:identifier ?id .

    MINUS {
        VALUES ?isValidated { "Validated" }
    }
}