package fr.insee.rmes.bauhaus_services.rdfUtils;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.config.auth.security.restrictions.StampsRestrictionsService;

public abstract class RdfService {

	@Autowired
	protected RepositoryGestion repoGestion;
	
	@Autowired
	protected StampsRestrictionsService stampsRestrictionsService;
}
