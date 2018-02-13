package fr.insee.rmes.persistance.securityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultSecurityManagerImpl implements SecurityManagerContract {
	
	final static Logger logger = LogManager.getLogger(DefaultSecurityManagerImpl.class);
	
	public String getAuth(String body) {
		return "";
	}
	
	public String getRoles() {
		return "";
	}
	
	public String getAgents() {
		return "";
	}
	
	public void setAddRole(String body) {
		
	}
	
	public void setDeleteRole(String body) {
		
	}

}
