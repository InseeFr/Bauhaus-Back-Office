package fr.insee.rmes.bauhaus_services.rdf_utils;

import fr.insee.rmes.config.freemarker.FreemarkerConfig;
import fr.insee.rmes.exceptions.RmesException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class FreeMarkerUtils {

	static final Logger logger = LoggerFactory.getLogger(FreeMarkerUtils.class);

	
	private FreeMarkerUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String buildRequest(String root, String fileName, Map<String, Object> params) throws RmesException {
		logger.debug("Execute query {}{} ", root,fileName);
		Template temp;
		Writer out = new StringWriter();
		try {
			temp = FreemarkerConfig.getCfg().getTemplate(root + fileName);
			temp.process(params, out);
		} catch (IOException | TemplateException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
					"Can't read query " + fileName);
		}
		return out.toString();
	}

}
