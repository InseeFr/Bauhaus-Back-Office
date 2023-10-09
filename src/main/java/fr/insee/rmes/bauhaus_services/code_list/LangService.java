package fr.insee.rmes.bauhaus_services.code_list;

import fr.insee.rmes.bauhaus_services.CodeListService;
import fr.insee.rmes.bauhaus_services.rdf_utils.RdfService;
import fr.insee.rmes.exceptions.RmesException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LangService extends RdfService {

	private String language1;
	private String language2;
	
	
	@Autowired
	protected CodeListService codeListService;
	

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
	
	public String getLanguageByConfigLg(String lg) throws RmesException {
	if (lg==null || lg.equals("")) {
			return "";
	}
		return lg.equals(config.getLg1()) ? getLanguage1() : getLanguage2();
	}
	

	private String getLanguage(String lang) throws RmesException {
		return codeListService.getCodeUri("ISO-639", StringUtils.lowerCase(lang));
	}

}
