package fr.insee.rmes.external.services.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class ClasspathUriResolver implements URIResolver {

	static final Logger logger = LoggerFactory.getLogger(ClasspathUriResolver.class);

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		logger.debug("Resolving URI with href: {} and base: {}",href, base);
		String resolvedHref;
		if (href.startsWith("..")) {
			if (href.startsWith("../..")) {
				resolvedHref = href.replaceFirst("../..", "/xslt");
				logger.debug("Resolved URI is: {}" , resolvedHref);
			} else {
				resolvedHref = href.replaceFirst("..", "/xslt");
				logger.debug("Resolved XSLT URI is: {}" , resolvedHref);
			}
		} else {
			resolvedHref = href;
			logger.debug("Resolved URI href is: {}", resolvedHref);
		}
		return new StreamSource(ClasspathUriResolver.class.getResourceAsStream(resolvedHref));
	}

}
