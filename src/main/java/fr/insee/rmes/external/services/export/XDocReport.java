package fr.insee.rmes.external.services.export;

import fr.insee.rmes.bauhaus_services.operations.operations.VarBookExportBuilder;
import fr.insee.rmes.exceptions.RmesException;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import freemarker.ext.dom.NodeModel;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

@Component
public record XDocReport(VarBookExportBuilder varBookExport) {

	static final Logger logger = LoggerFactory.getLogger(XDocReport.class);


	public byte[] exportVariableBookInOdt(String xml, File odtTemplate) throws RmesException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IXDocReport report;


		// 1) Load DOCX into XWPFDocument
		try {
			report = getReportTemplate(odtTemplate);
			
			// 2) Create Java model context 
			IContext context = getXmlData(report, xml);
	
			report.process(context, baos);
		}catch (IOException | XDocReportException e) {
			logger.error(e.getMessage());
		}
		return baos.toByteArray();
	}



	private IContext getXmlData(IXDocReport report, String xmlInput)
			throws  RmesException {

		String xmlString = varBookExport.getData(xmlInput);
		InputStream projectInputStream = new ByteArrayInputStream(xmlString.getBytes());
		InputSource projectInputSource = new InputSource( projectInputStream );
		NodeModel xml = null;
		IContext context = null ;
		try {
			xml = NodeModel.parse( projectInputSource );
			context = report.createContext();
		} catch (SAXException | IOException | ParserConfigurationException | XDocReportException e) {
			throw new RmesException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e.getClass()+" - Can't put xml in XdocReport context");
		}
		context.put("racine", xml);
		return context;
	}

	private IXDocReport getReportTemplate(File odtTemplate) throws IOException, XDocReportException {
		try(InputStream is = new FileInputStream(odtTemplate)){
			return  XDocReportRegistry.getRegistry().loadReport(is,TemplateEngineKind.Freemarker);
		}

	}

}
