SELECT ?id ?masLabelLg1 ?masLabelLg2 ?range ?sansObject ?isPresentational ?maxOccurs ?codeList
  FROM <${MSD_GRAPH}> 
  FROM <${CODELIST_GRAPH}> 
  FROM <${MSD_CONCEPTS_GRAPH}>
  WHERE { 
	  ?mas rdf:type sdmx-mm:MetadataAttributeSpecification
	  OPTIONAL {?mas sdmx-mm:isPresentational ?isPresentational } 
	  OPTIONAL {?mas sdmx-mm:maxOccurs ?maxOccurs}
									
	  ?mas sdmx-mm:metadataAttributeProperty ?map .
      	  {
      	    ?map rdfs:range/owl:UnionOf/(rdf:rest)*/rdf:first ?range .
      	    BIND(true AS ?sansObject)
      		FILTER (?range != insee:RubriqueSimsSansObjet)
      	  }
      	  UNION
      	  {
      	    ?map rdfs:range ?range .
      	    BIND(false AS ?sansObject)
      	  }
      FILTER (!isBlank(?range))

      OPTIONAL {?range rdfs:seeAlso/skos:notation|skos:notation ?codeList .}

	  
	  ?map sdmx-mm:concept ?concept .
	  ?concept skos:prefLabel ?masLabelLg1 ; 
	  		   skos:prefLabel ?masLabelLg2 ; 
	  FILTER(lang(?masLabelLg1) = '${LG1}') 
	  FILTER(lang(?masLabelLg2) = '${LG2}') 
									
<#if uniqueAttr = "true">
	FILTER(STRENDS(STR(?mas), '${idMas}')) 
<#else>
	?mas  dc:identifier ?id .
</#if>	
  } 
  ORDER BY ?mas