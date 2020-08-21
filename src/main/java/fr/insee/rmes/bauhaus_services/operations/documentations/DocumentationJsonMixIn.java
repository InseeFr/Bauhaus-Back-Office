package fr.insee.rmes.bauhaus_services.operations.documentations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public abstract class DocumentationJsonMixIn {
	  @JsonCreator
	    public DocumentationJsonMixIn(
	        String id,
	         String idOperation,
	    	 String idSeries,
	    	 String idIndicator,

	    	 String labelLg1,
	    	 String labelLg2
	  ) { }

	    
	    @JsonInclude(Include.NON_EMPTY)
	    abstract String getLabelLg1();

}
