package fr.insee.rmes.config.swagger.model.application;

import io.swagger.v3.oas.annotations.media.Schema;

public class Init {
	
	@Schema(description = "Base host URL of front-end", required = true)
	public String appHost;

	@Schema(description = "Base host URL of the authorization service", required = true)
	public String authorizationHost;
	
	@Schema(description = "Default concept contributor", required = true)
	public String defaultContributor;
	
	@Schema(description = "Mail sender adress", required = true)
	public String defaultMailSender;
	
	@Schema(description = "Max length of concept scope note", required = true)
	public String maxLengthScopeNote;
	
	@Schema(description = "First lang of application", required = true)
	public String lg1;
	
	@Schema(description = "Second lang of application", required = true)
	public String lg2;
	
	@Schema(description = "Type of authentication", allowableValues =  {"NoAuthImpl,BasicAuthImpl,OpenIDConnectAuth"})
	public String authType;

}
