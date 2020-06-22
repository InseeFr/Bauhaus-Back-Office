package fr.insee.rmes.bauhaus_services.operations.documentations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import fr.insee.rmes.model.operations.documentations.DocumentationRubric;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public abstract class DocumentationJsonMixIn {
	  @JsonCreator
	    public DocumentationJsonMixIn(
	        String id,
	         String idOperation,
	    	 String idSeries,
	    	 String idIndicator,

	    	 String labelLg1,
	    	 String labelLg2
	    //	 List<DocumentationRubric> rubrics; 
	  ) { }
//
//	    @JsonUnwrapped
//	    abstract String getIntituleSansArticle();
	    
	    @JsonInclude(Include.NON_EMPTY)
	    abstract String getLabelLg1();

}
