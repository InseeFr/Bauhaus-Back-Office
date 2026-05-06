SELECT DISTINCT ?key ?stamp ?label
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
    {
        # Input is a legacy stamp literal: resolve to organization IRI via adms:identifier
        VALUES ?stamp {
            <#list IDENTIFIERS as identifier>
            "${identifier}"
            </#list>
        }
        ?organization adms:identifier ?stamp .
        OPTIONAL { ?organization skos:prefLabel ?labelLang . FILTER(lang(?labelLang) = '${LANG}') }
        OPTIONAL { ?organization skos:prefLabel ?labelAny . }
        BIND(COALESCE(?labelLang, ?labelAny) AS ?label)
        BIND(?stamp AS ?key)
    }
    UNION
    {
        # Input is already an organization IRI; adms:identifier may be absent on sub-units
        # and skos:prefLabel may not be tagged with the requested language.
        VALUES ?organization {
            <#list IDENTIFIERS as identifier>
            <${identifier}>
            </#list>
        }
        OPTIONAL { ?organization adms:identifier ?stamp . }
        OPTIONAL { ?organization skos:prefLabel ?labelLang . FILTER(lang(?labelLang) = '${LANG}') }
        OPTIONAL { ?organization skos:prefLabel ?labelAny . }
        BIND(COALESCE(?labelLang, ?labelAny) AS ?label)
        BIND(STR(?organization) AS ?key)
    }
    FILTER(BOUND(?label))
}
