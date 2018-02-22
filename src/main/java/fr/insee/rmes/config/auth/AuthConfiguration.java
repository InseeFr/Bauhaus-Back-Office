package fr.insee.rmes.config.auth;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import fr.insee.rmes.config.auth.conditions.BasicAuthCondition;
import fr.insee.rmes.config.auth.conditions.FakeAuthCondition;
import fr.insee.rmes.config.auth.conditions.NoAuthCondition;
import fr.insee.rmes.config.auth.conditions.OpenIDConnectAuthCondition;
import fr.insee.rmes.persistance.securityManager.BasicAuthImpl;
import fr.insee.rmes.persistance.securityManager.FakeAuthImpl;
import fr.insee.rmes.persistance.securityManager.NoAuthImpl;
import fr.insee.rmes.persistance.securityManager.OpenIDConnectAuthImpl;
import fr.insee.rmes.persistance.securityManager.SecurityManagerService;


@Configuration
public class AuthConfiguration {
	
	@Bean(name = "securityManagerService")
	@Conditional(value = OpenIDConnectAuthCondition.class)
	public SecurityManagerService getOpenIDConnectAuthImpl() {
		return new OpenIDConnectAuthImpl();
	}
	
	@Bean(name = "securityManagerService")
	@Conditional(value = BasicAuthCondition.class)
	public SecurityManagerService getBasicAuthImpl() {
		return new BasicAuthImpl();
	}
	
	@Bean(name = "securityManagerService")
	@Conditional(value = FakeAuthCondition.class)
	public SecurityManagerService getFakeAuthImpl() {
		return new FakeAuthImpl();
	}
	
	@Bean(name = "securityManagerService")
	@Conditional(value = NoAuthCondition.class)
	public SecurityManagerService getNoAuthImpl() {
		return new NoAuthImpl();
	}

}
