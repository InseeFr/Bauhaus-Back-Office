SELECT ?id ?idDataset ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?created ?updated ?format ?taille ?url
FROM <${DATASET_GRAPH}>
WHERE {
	?uri rdf:type dcat:Distribution .
    
    ?uri dcterms:identifier ?id .
    
    ?uriDataset rdf:type dcat:Dataset .
	?uriDataset dcat:distribution ?uri .

<#if DATASET_ID != "">
	?uriDataset dcterms:identifier '${DATASET_ID}' .
</#if>

    ?uriDataset dcterms:identifier ?idDataset .
    
	OPTIONAL {
		?uri dcterms:title ?labelLg1
		FILTER (lang(?labelLg1) = '${LG1}') .
    }

	OPTIONAL {
		?uri dcterms:title ?labelLg2
		FILTER (lang(?labelLg2) = '${LG2}') .
	}
    
	OPTIONAL {
        ?uri dcterms:description ?descriptionLg1
        FILTER (lang(?descriptionLg1) = '${LG1}') .
    }
    
	OPTIONAL {
        ?uri dcterms:description ?descriptionLg2
        FILTER (lang(?descriptionLg2) = '${LG2}') .
	}

	OPTIONAL {
		?uri dcterms:created ?created .
	}

	OPTIONAL {
		?uri dcterms:modified ?updated .
	}

	OPTIONAL {
		?uri dcterms:format ?format .
	}

	OPTIONAL {
		?uri dcat:byteSize ?taille .
	}

	OPTIONAL {
		?uri dcat:downloadURL ?url .
	}

    OPTIONAL {
        ?uriDataset insee:validationState ?validationState .
    }
}