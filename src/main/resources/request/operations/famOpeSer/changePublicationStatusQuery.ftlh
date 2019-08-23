WITH <http://rdf.insee.fr/graphes/operations>

DELETE {
	<${famopeserURI}> insee:validationState ?oldState 
		}

;

WITH <http://rdf.insee.fr/graphes/operations>

INSERT {
	<${famopeserURI}>  insee:validationState <${newState}>
	}

	 
	 