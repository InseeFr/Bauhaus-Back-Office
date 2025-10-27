package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.Config;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LangService {

	private String language1;
	private String language2;


	private final CodeListService codeListService;
	private final Config config;

	public LangService(CodeListService codeListService, Config config) {
		this.codeListService = codeListService;
		this.config = config;
	}

	/**
	 * Get psi.oasis language for the two technical language (cf. properties)
	 * @return
	 * @throws RmesException
	 */
	public String getLanguage1() throws RmesException {
		if (language1 == null) {
			language1 = getLanguage(config.getLg1());
		}
		return language1;
	}

	public String getLanguage2() throws RmesException {
		if (language2 == null) {
			language2 = getLanguage(config.getLg2());
		}
		return language2;
	}


	private String getLanguage(String lang) throws RmesException {
		return codeListService.getCodeUri("ISO-639", StringUtils.lowerCase(lang));
	}

}
