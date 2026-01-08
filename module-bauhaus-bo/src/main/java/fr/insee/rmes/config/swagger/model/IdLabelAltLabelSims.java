package fr.insee.rmes.config.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;

public class IdLabelAltLabelSims {

		@Schema(description = "Id", requiredMode = Schema.RequiredMode.REQUIRED)
		public String id;
		
		@Schema(description = "Label", requiredMode = Schema.RequiredMode.REQUIRED)
		public String label;
		
		@Schema(description = "Alternative value")
		public String altLabel;

		@Schema(description = "Documentation")
		public String idSims;
}
