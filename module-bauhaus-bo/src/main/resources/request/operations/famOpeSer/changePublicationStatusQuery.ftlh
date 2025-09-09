WITH <${OPERATIONS_GRAPH}>

DELETE {
	<${famopeserURI}> insee:validationState ?oldState 
		}

;

WITH <${OPERATIONS_GRAPH}>

INSERT {
	<${famopeserURI}>  insee:validationState <${newState}>
	}

	 
	 