SELECT DISTINCT ?key ?stamp ?label
FROM <${ORGANIZATIONS_GRAPH}>
FROM <${ORGANIZATIONS_INSEE_GRAPH}>
WHERE {
<#if (LITERAL_IDENTIFIERS?size > 0) && (IRI_IDENTIFIERS?size > 0)>
    {
<#elseif (LITERAL_IDENTIFIERS?size > 0)>
    {
</#if>
<#if (LITERAL_IDENTIFIERS?size > 0)>
        # Input is a legacy stamp literal: resolve to organization IRI via adms:identifier
        VALUES ?stamp {
            <#list LITERAL_IDENTIFIERS as identifier>
            "${identifier}"
            </#list>
        }
        ?organization adms:identifier ?stamp .
        OPTIONAL { ?organization skos:prefLabel ?labelLang . FILTER(lang(?labelLang) = '${LANG}') }
        OPTIONAL { ?organization skos:prefLabel ?labelAny . }
        BIND(COALESCE(?labelLang, ?labelAny) AS ?label)
        BIND(?stamp AS ?key)
</#if>
<#if (LITERAL_IDENTIFIERS?size > 0) && (IRI_IDENTIFIERS?size > 0)>
    }
    UNION
    {
<#elseif (IRI_IDENTIFIERS?size > 0)>
    {
</#if>
<#if (IRI_IDENTIFIERS?size > 0)>
        # Input is already an organization IRI; adms:identifier may be absent on sub-units
        # and skos:prefLabel may not be tagged with the requested language.
        VALUES ?organization {
            <#list IRI_IDENTIFIERS as identifier>
            <${identifier}>
            </#list>
        }
        OPTIONAL { ?organization adms:identifier ?stamp . }
        OPTIONAL { ?organization skos:prefLabel ?labelLang . FILTER(lang(?labelLang) = '${LANG}') }
        OPTIONAL { ?organization skos:prefLabel ?labelAny . }
        BIND(COALESCE(?labelLang, ?labelAny) AS ?label)
        BIND(STR(?organization) AS ?key)
</#if>
<#if (LITERAL_IDENTIFIERS?size > 0) || (IRI_IDENTIFIERS?size > 0)>
    }
</#if>
    FILTER(BOUND(?label))
}
