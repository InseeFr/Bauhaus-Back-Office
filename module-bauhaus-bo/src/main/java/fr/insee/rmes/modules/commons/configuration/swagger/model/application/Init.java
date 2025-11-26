package fr.insee.rmes.modules.commons.configuration.swagger.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

public class Init {
	
	@Schema(description = "Base host URL of front-end", requiredMode = Schema.RequiredMode.REQUIRED)
	public String appHost;

	@Schema(description = "Base host URL of the authorization service", requiredMode = Schema.RequiredMode.REQUIRED)
	public String authorizationHost;
	
	@Schema(description = "Default concept contributor", requiredMode = Schema.RequiredMode.REQUIRED)
	public String defaultContributor;
	
	@Schema(description = "Mail sender adress", requiredMode = Schema.RequiredMode.REQUIRED)
	public String defaultMailSender;
	
	@Schema(description = "Max length of concept scope note", requiredMode = Schema.RequiredMode.REQUIRED)
	public String maxLengthScopeNote;
	
	@Schema(description = "First lang of application", requiredMode = Schema.RequiredMode.REQUIRED)
	public String lg1;
	
	@Schema(description = "Second lang of application", requiredMode = Schema.RequiredMode.REQUIRED)
	public String lg2;
	
	@Schema(description = "Type of authentication", allowableValues =  {"NoAuthImpl,BasicAuthImpl,OpenIDConnectAuth"})
	public String authType;

}
