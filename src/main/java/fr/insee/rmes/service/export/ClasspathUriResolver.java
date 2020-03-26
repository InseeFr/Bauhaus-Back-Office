package fr.insee.rmes.service.export;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClasspathUriResolver implements URIResolver {

	final static Logger logger = LoggerFactory.getLogger(ClasspathUriResolver.class);

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		logger.debug("Resolving URI with href: " + href + " and base: " + base);
		String resolvedHref;
		if (href.startsWith("..")) {
			if (href.startsWith("../..")) {
				resolvedHref = href.replaceFirst("../..", "/xslt");
				logger.debug("Resolved URI is: " + resolvedHref);
			} else {
				resolvedHref = href.replaceFirst("..", "/xslt");
				logger.debug("Resolved XSLT URI is: " + resolvedHref);
			}
		} else {
			resolvedHref = href;
			logger.debug("Resolved URI href is: " + resolvedHref);
		}
		return new StreamSource(ClasspathUriResolver.class.getResourceAsStream(resolvedHref));
	}

}
