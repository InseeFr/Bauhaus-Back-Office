SELECT distinct ?id ?uri ?url ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?updatedDate ?lang
	FROM <${DOCUMENTS_GRAPH}>

<#if idSims != "">
	FROM <${DOCUMENTATIONS_GRAPH}/${idSims}>
</#if>	

WHERE { 

<#if idSims != "">
	 ?text rdf:type dcmitype:Text .
	 ?text insee:additionalMaterial ?uri . 
	 <#if LANG != "">
	 	?text dcterms:language <${LANG}> .
	 </#if>
</#if>	

	 ?uri rdf:type foaf:Document .
	 ?uri <http://schema.org/url> ?url .
 
 	 OPTIONAL{ ?uri rdfs:label ?labelLg1 . 
		FILTER(lang(?labelLg1) = '${LG1}') 
	 }
	 OPTIONAL{ ?uri rdfs:label ?labelLg2 . 
		FILTER(lang(?labelLg2) = '${LG2}')  
	 }
	 
	 OPTIONAL{ ?uri rdfs:comment ?descriptionLg1 . 
		FILTER(lang(?descriptionLg1) = '${LG1}') 
	 }
	 OPTIONAL{ ?uri rdfs:comment ?descriptionLg2 . 
		FILTER(lang(?descriptionLg2) = '${LG2}')  
	 }
	 
	 OPTIONAL{ ?uri pav:lastRefreshedOn ?updatedDate .  }
	 OPTIONAL{ ?uri dc:language ?lang .  }
	 
	 
<#if id != "">
		FILTER(REPLACE( STR(?uri) , '(.*/)(\\w.+$)', '$2' ) = '${id}')
	    BIND('${id}' AS ?id) .
<#else>
	    BIND(REPLACE( STR(?uri) , '(.*/)(\\w.+$)', '$2' ) AS ?id) .
</#if>

<#if idRubric != "">
	FILTER(REGEX(STR(?text), '${idRubric}')) 
</#if>	
<#if type != "">
	FILTER(REGEX(STR(?uri), '${type}')) 
</#if>	

 }