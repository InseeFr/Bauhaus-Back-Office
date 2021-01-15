ASK
FROM<${STRUCTURES_COMPONENTS_GRAPH}>
FROM<${STRUCTURES_GRAPH}>
{
    FILTER(?nbComponent = ${NB_COMPONENT} && ?nbShared = ?nbComponent )
    {
        SELECT ?dsd (COUNT(?componentSpec) AS ?nbComponent) (COUNT(?id) AS ?nbShared)
        {
            ?dsd a qb:DataStructureDefinition .
            ?dsd qb:component ?componentSpec .
            OPTIONAL {
                ?componentSpec (qb:attribute | qb:dimension | qb:measure) / dcterms:identifier ?id .
                FILTER(
<#list IDS as id>
?id = "${id}" <#sep>
</#list>
 )
            }
            FILTER NOT EXISTS {
                ?dsd dcterms:identifier '${STRUCTURE_ID}' .
            }
        }
        GROUP BY ?dsd
    }
}