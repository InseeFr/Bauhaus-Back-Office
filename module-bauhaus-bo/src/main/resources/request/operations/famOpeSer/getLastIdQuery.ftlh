# Dernier identifiant du compteur partagé des familles, séries et opérations.
# L'identifiant est le dernier segment de l'IRI : RdfUtils.objectIRI le construit ainsi.
# Les exports historiques n'ont pas d'adms:identifier, on ne peut donc pas s'appuyer dessus.
# Le FILTER écarte tout ce qui n'est pas un identifiant, sans quoi xsd:integer échoue et ?id
# reste non lié.
SELECT ?id
FROM <${OPERATIONS_GRAPH}>
WHERE {
    VALUES ?type { insee:StatisticalOperationFamily insee:StatisticalOperationSeries insee:StatisticalOperation }
    ?s a ?type .
    BIND (REPLACE(REPLACE(STR(?s), "^.*/", ""), "^s", "") AS ?numericPart)
    FILTER (REGEX(?numericPart, "^[0-9]+$"))
    BIND (xsd:integer(?numericPart) AS ?id)
}
ORDER BY DESC(?id)
LIMIT 1
