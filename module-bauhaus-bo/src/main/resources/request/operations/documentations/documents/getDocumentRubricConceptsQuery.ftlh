SELECT DISTINCT ?concept
WHERE {
	GRAPH ?simsGraph {
		?rubric insee:additionalMaterial/rdf:rest*/rdf:first ${URI} .
		?attribute a sdmx-mm:ReportedAttribute ;
			?property ?rubric .
	}
	GRAPH ${MSD_GRAPH} {
		?property sdmx-mm:concept ?concept .
	}
}
