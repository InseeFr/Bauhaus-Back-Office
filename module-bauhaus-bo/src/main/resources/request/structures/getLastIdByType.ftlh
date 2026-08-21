# Dernier identifiant des composants mutualisés du type demandé.
# On lit dcterms:identifier — écrit par StructureComponentRepository et exigé par getMutualizedComponents —
# plutôt que de découper l'IRI : le graphe contient d'autres ressources du même rdf:type qui ne sont
# pas des composants mutualisés. Le FILTER écarte les identifiants non numériques, sans quoi
# xsd:integer échoue et ?id reste non lié.
SELECT ?id
FROM ${STRUCTURES_COMPONENTS_GRAPH}
WHERE {
	?s a ${TYPE} ;
	   dcterms:identifier ?identifier .
	BIND (REPLACE(STR(?identifier), ${ID_PREFIX_PATTERN}, "") AS ?numericPart)
	FILTER (REGEX(?numericPart, "^[0-9]+$"))
	BIND (xsd:integer(?numericPart) AS ?id)
}
ORDER BY DESC(?id)
LIMIT 1
