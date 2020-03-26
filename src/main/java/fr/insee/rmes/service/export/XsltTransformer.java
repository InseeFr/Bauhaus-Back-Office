package fr.insee.rmes.service.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.io.TempFileCache;

import fr.insee.rmes.exceptions.BauhausErrorListener;

public class XsltTransformer {

	private static final Logger logger = LoggerFactory.getLogger(XsltTransformer.class);

	/**
	 * Main Saxon transformation method
	 * 
	 * @param transformer
	 *            : The defined transformer with his embedded parameters (defined in
	 *            the other methods of this class)
	 * @param xmlInput
	 *            : The input xml file where the XSLT will be applied
	 * @param xmlOutput
	 *            : The output xml file after the transformation
	 * @throws Exception
	 *             : Mainly if the input/output files path are incorrect
	 */
	public void xslTransform(Transformer transformer, InputStream xmlInput, OutputStream xmlOutput) throws Exception {
		logger.debug("Starting xsl transformation -Input : " + xmlInput + " -Output : " + xmlOutput);
		transformer.transform(new StreamSource(xmlInput), new StreamResult(xmlOutput));
	}

	/**
	 * Basic Transformer initialization without parameters
	 * 
	 * @param input
	 *            : the input xml file
	 * @param xslSheet
	 *            : the xsl stylesheet that will be used
	 * @param output
	 *            : the xml output that will be created
	 * @throws Exception
	 *             : if the factory couldn't be found or if the paths are incorrect
	 */
	public void transform(InputStream input, InputStream xslSheet, OutputStream output) throws Exception {
		logger.debug("Using the basic transformer");
		TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
		tFactory.setURIResolver(new ClasspathUriResolver());
		Transformer transformer = tFactory.newTransformer(new StreamSource(xslSheet));
		transformer.setErrorListener(new BauhausErrorListener());
		xslTransform(transformer, input, output);
	}
	
	public static final String IN2OUT_PROPERTIES_FILE = "properties-file";
	public static final String IN2OUT_PARAMETERS_FILE = "parameters-file";
	public static final String PARAMETERS = "/parameters.xml";
	public static final String IN2OUT_LABELS_FOLDER = "labels-folder";
	public static final String IN2OUT_PARAMETERS_NODE = "parameters-node";


	
	private void transformIn2Out(InputStream inputFile, OutputStream outputFile, InputStream xslSheet,
			byte[] parameters, String propertiesFile) throws Exception {
		InputStream parametersIS = null;
		TransformerFactory tFactory = new net.sf.saxon.TransformerFactoryImpl();
		tFactory.setURIResolver(new ClasspathUriResolver());
		Transformer transformer = tFactory.newTransformer(new StreamSource(xslSheet));
		transformer.setErrorListener(new BauhausErrorListener());
		transformer.setParameter(IN2OUT_PROPERTIES_FILE, propertiesFile);
		transformer.setParameter(IN2OUT_PARAMETERS_FILE, PARAMETERS);
		if (parameters != null) {
			logger.info("Using specifics parameters");
			parametersIS = new ByteArrayInputStream(parameters);
			Source source = new StreamSource(parametersIS);
			transformer.setParameter(IN2OUT_PARAMETERS_NODE, source);
		}
		transformer.setParameter(IN2OUT_LABELS_FOLDER, new TempFileCache("toto"));
		logger.debug(String.format("Transformer parameters are: %s, %s",
				transformer.getParameter(IN2OUT_PROPERTIES_FILE),
				transformer.getParameter(IN2OUT_PARAMETERS_FILE),
				transformer.getParameter(IN2OUT_LABELS_FOLDER)));
		xslTransform(transformer, inputFile, outputFile);
		if (parameters != null) {
			parametersIS.close();
		}
	}
}
