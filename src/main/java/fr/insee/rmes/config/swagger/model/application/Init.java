package fr.insee.rmes.config.swagger.model.application;

import io.swagger.annotations.ApiModelProperty;

public class Init {
	
	@ApiModelProperty(value = "Base host URL of front-end", required = true)
	public String appHost;
	
	@ApiModelProperty(value = "Default concept contributor", required = true)
	public String defaultContributor;
	
	@ApiModelProperty(value = "Mail sender adress", required = true)
	public String defaultMailSender;
	
	@ApiModelProperty(value = "Max length of concept scope note", required = true)
	public String maxLengthScopeNote;
	
	@ApiModelProperty(value = "First lang of application", required = true)
	public String lg1;
	
	@ApiModelProperty(value = "Second lang of application", required = true)
	public String lg2;
	
	@ApiModelProperty(value = "Type of authentication", required = true, allowableValues = "NoAuthImpl,BasicAuthImpl,OpenIDConnectAuth")
	public String authType;

}
