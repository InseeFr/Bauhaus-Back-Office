package fr.insee.rmes.config.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class IdLabelAltLabelSims {

		@Schema(description = "Id", required = true)
		public String id;
		
		@Schema(description = "Label", required = true)
		public String label;
		
		@Schema(description = "Alternative label")
		public String altLabel;

		@Schema(description = "Documentation")
		public String idSims;
}
