SELECT ?id ?idDataset ?labelLg1 ?labelLg2 ?descriptionLg1 ?descriptionLg2 ?created ?updated ?validationState
?format ?byteSize ?url
FROM <${DATASET_GRAPH}>
WHERE {
	?uri rdf:type dcat:Distribution .
    
    ?uri dcterms:identifier '${ID}' .
    ?uri dcterms:identifier ?id .

	OPTIONAL {
		?uriDataset rdf:type dcat:Dataset .
		?uriDataset dcat:distribution ?uri .

		?uriDataset dcterms:identifier ?idDataset .
	}

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
		?uri dcat:byteSize ?byteSize .
	}

	OPTIONAL {
		?uri dcat:downloadURL ?url .
	}

    OPTIONAL {
        ?uri insee:validationState ?validationState .
    }
}