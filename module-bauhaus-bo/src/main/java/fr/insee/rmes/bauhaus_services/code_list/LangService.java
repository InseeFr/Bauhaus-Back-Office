package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.BauhausLanguagesProperties;
import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.domain.exceptions.RmesException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LangService {

	private String language1;
	private String language2;


	private final CodeListService codeListService;
    private final BauhausLanguagesProperties languages;

	public LangService(CodeListService codeListService, BauhausLanguagesProperties languages) {
		this.codeListService = codeListService;
        this.languages = languages;
	}

	/**
	 * Get psi.oasis language for the two technical language (cf. properties)
	 * @return
	 * @throws RmesException
	 */
	public String getLanguage1() throws RmesException {
		if (language1 == null) {
			language1 = getLanguage(languages.lg1());
		}
		return language1;
	}

	public String getLanguage2() throws RmesException {
		if (language2 == null) {
			language2 = getLanguage(languages.lg2());
		}
		return language2;
	}


	private String getLanguage(String lang) throws RmesException {
		return codeListService.getCodeUri("ISO-639", StringUtils.lowerCase(lang));
	}

}
