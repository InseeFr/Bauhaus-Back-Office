SELECT DISTINCT 
	?id ?prefLabelLg1 ?prefLabelLg2 
	?created ?modified 
	?altLabelLg1 ?altLabelLg2 
	?abstractLg1 ?abstractLg2  
	?historyNoteLg1 ?historyNoteLg2 
	?idSims  
	?typeCode ?typeList  ?accrualPeriodicityCode ?accrualPeriodicityList  
	?hasPublisher ?hasContributor  ?hasCreator ?hasDataCollector
	
WHERE {  
	?series a insee:StatisticalOperationSeries .
	BIND(STRAFTER(STR(?series),'/operations/serie/') AS ?id) . 
	
<#if stamp != ""> 
		?series dc:creator ?crea .
		FILTER (str(?crea) = '${stamp}' )  .  
</#if>	
	
	?series skos:prefLabel ?prefLabelLg1 .
	FILTER (lang(?prefLabelLg1) = '${LG1}') 

 	OPTIONAL{
 		?series skos:prefLabel ?prefLabelLg2 .
		FILTER (lang(?prefLabelLg2) = '${LG2}') 
	} 
 	OPTIONAL { ?series dcterms:created ?created } .  
	OPTIONAL { ?series dcterms:modified ?modified } .  
 	OPTIONAL{
 		?series skos:altLabel ?altLabelLg1 
		FILTER (lang(?altLabelLg1) = '${LG1}') 
	} 
 	OPTIONAL{
 		?series skos:altLabel ?altLabelLg2 
		FILTER (lang(?altLabelLg2) = '${LG2}') 
	} 
 	OPTIONAL{
 		?series dcterms:abstract ?abstractLg1 
		FILTER (lang(?abstractLg1) = '${LG1}') 
	} 
 	OPTIONAL{
 		?series dcterms:abstract ?abstractLg2 
		FILTER (lang(?abstractLg2) = '${LG2}') 
	} 
 	OPTIONAL{
 		?series skos:historyNote ?historyNoteLg1 
		FILTER (lang(?historyNoteLg1) = '${LG1}') 
	} 
 	OPTIONAL{
 		?series skos:historyNote ?historyNoteLg2 
		FILTER (lang(?historyNoteLg2) = '${LG2}') 
	} 
 	OPTIONAL{ 
 		?report rdf:type sdmx-mm:MetadataReport . 
 		?report sdmx-mm:target ?series  
 		BIND(STRAFTER(STR(?report),'/rapport/') AS ?idSims) . 
	} 
	OPTIONAL {
		?series dcterms:type ?type . 
		?type skos:notation ?typeCode . 
		?type skos:inScheme ?typeCodeList . 
		?typeCodeList skos:notation ?typeList . 
	}   
	OPTIONAL {
		?series dcterms:accrualPeriodicity ?accrualPeriodicity . 
		?accrualPeriodicity skos:notation ?accrualPeriodicityCode . 
		?accrualPeriodicity skos:inScheme ?accrualPeriodicityCodeList . 
		?accrualPeriodicityCodeList skos:notation ?accrualPeriodicityList . 
	}   
	
	BIND(EXISTS{?series dc:creator ?creator} AS ?hasCreator) 
	BIND(EXISTS{?series dcterms:contributor ?contrib} AS ?hasContributor) 
	BIND(EXISTS{?series insee:dataCollector ?datacoll} AS ?hasDataCollector) 
	BIND(EXISTS{?series dcterms:publisher ?publisher} AS ?hasPublisher) 
	          		
} 
