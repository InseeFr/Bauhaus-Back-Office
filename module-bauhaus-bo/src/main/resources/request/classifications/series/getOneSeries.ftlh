SELECT ?id ?prefLabelLg1 ?prefLabelLg2 ?altLabelLg1 ?altLabelLg2
?scopeNoteLg1 ?scopeNoteLg2 ?subject ?publishers ?covers ?familyLg1 ?familyLg2 ?idFamily
WHERE { GRAPH${GRAPH} {
?series skos:prefLabel ?prefLabelLg1 .
FILTER(REGEX(STR(?series),${SERIES_URI_PATTERN})) .
BIND(STRAFTER(STR(?series),'/codes/serieDeNomenclatures/') AS ?id) .
FILTER (lang(?prefLabelLg1) = ${LG1}) .
OPTIONAL {?series skos:prefLabel ?prefLabelLg2 .
FILTER (lang(?prefLabelLg2) = ${LG2}) } .
{OPTIONAL{
SELECT (group_concat(?altLg1;separator=' || ') as ?altLabelLg1) WHERE {
?series skos:altLabel ?altLg1 .
FILTER (lang(?altLg1) = ${LG1})  .
FILTER(REGEX(STR(?series),${SERIES_CODES_URI_PATTERN})) .
}}}
{OPTIONAL{
SELECT (group_concat(?altLg2;separator=' || ') as ?altLabelLg2) WHERE {
?series skos:altLabel ?altLg2 .
FILTER (lang(?altLg2) = ${LG2})  .
FILTER(REGEX(STR(?series),${SERIES_CODES_URI_PATTERN})) .
}}}
OPTIONAL {?series dc:subject ?subject } .
OPTIONAL {?series dc:publisher ?publishers } .
OPTIONAL {?series xkos:covers ?covers } .
<#-- Remarque lg1 -->
OPTIONAL {?series skos:scopeNote ?scopeLg1 .
?scopeLg1 dcterms:language ${LG1}^^xsd:language .
?scopeLg1 evoc:noteLiteral ?scopeNoteLg1 .
} .
<#-- Remarque lg2 -->
OPTIONAL {?series skos:scopeNote ?scopeLg2 .
?scopeLg2 dcterms:language ${LG2}^^xsd:language .
?scopeLg2 evoc:noteLiteral ?scopeNoteLg2 .
} .
OPTIONAL {?series xkos:belongsTo ?familyURI .
?familyURI skos:prefLabel ?familyLg1 .
FILTER (lang(?familyLg1) = ${LG1})  .
BIND(STRAFTER(STR(?familyURI),'/codes/familleDeNomenclatures/') AS ?idFamily) } .
OPTIONAL {?series xkos:belongsTo ?familyURI .
?familyURI skos:prefLabel ?familyLg2 .
FILTER (lang(?familyLg2) = ${LG2}) }  .
}}
LIMIT 1
