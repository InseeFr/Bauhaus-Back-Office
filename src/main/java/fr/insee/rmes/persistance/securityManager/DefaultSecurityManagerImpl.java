package fr.insee.rmes.persistance.securityManager;

import org.apache.log4j.Logger;

public class DefaultSecurityManagerImpl implements SecurityManagerContract {
	
	final static Logger logger = Logger.getLogger(DefaultSecurityManagerImpl.class);
	
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
