package fr.insee.rmes.persistance.service.sesame.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOdtReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimpleReportExportConfiguration;

public class Jasper {
	
	final static Logger logger = Logger.getLogger(Jasper.class);
	
	public InputStream exportConcept(JSONObject json, String acceptHeader)  {

		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_concept.jrxml");
		try {
			return export(json, is, acceptHeader);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;
	}

	public InputStream exportCollection(JSONObject json, String acceptHeader)  {

		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_collection.jrxml");
		try {
			return export(json, is, acceptHeader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static InputStream export(JSONObject json, InputStream is, String acceptHeader) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(
			    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	    Map<String, Object> jasperParams = null;
	    try {
	    	jasperParams = mapper.readValue(json.toString(), new TypeReference<Map<String,Object>>(){});
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    String defaultPDFFont = "Arial";

	    JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
	    JRPropertiesUtil jrPropertiesUtil = JRPropertiesUtil.getInstance(jasperReportsContext);
	    jrPropertiesUtil.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
	    jrPropertiesUtil.setProperty("net.sf.jasperreports.default.font.name", defaultPDFFont);

		JasperDesign jasperReportDesign;
		jasperReportDesign = JRXmlLoader.load(is);

		JasperReport jasperReport = JasperCompileManager.compileReport(jasperReportDesign);

		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParams, new JREmptyDataSource());

		// Export IS
//		Exporter<ExporterInput, PdfReportConfiguration, PdfExporterConfiguration, OutputStreamExporterOutput> exporter = new JRPdfExporter();
//		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//		SimplePdfReportConfiguration pdfReportConfiguration = new SimplePdfReportConfiguration();
//		exporter.setConfiguration(pdfReportConfiguration);
		@SuppressWarnings("rawtypes")
		Exporter exporter = getExporter(acceptHeader, jasperPrint);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        SimpleReportExportConfiguration config = getReport(acceptHeader);
        exporter.setConfiguration(config);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(output);
		exporter.setExporterOutput(exporterOutput);
		exporter.exportReport();
		InputStream in = new ByteArrayInputStream(output.toByteArray());
		
		return in;		
	}
	
	@SuppressWarnings("rawtypes")
	private static Exporter getExporter(String acceptHeader, JasperPrint jasperPrint) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
			System.out.println("ok");return new JROdtExporter();}
		else if (acceptHeader.equals("application/octet-stream")) return new JRPdfExporter();
		// default --> Odt
		else return new JROdtExporter();
	}
	
	private static SimpleReportExportConfiguration getReport(String acceptHeader) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) return new SimpleOdtReportConfiguration();
		else if (acceptHeader.equals("application/octet-stream")) return new SimplePdfReportConfiguration();
		// default --> Odt
		else return new SimpleOdtReportConfiguration();
	}
	
	public String getExtension(String acceptHeader) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) return ".odt";
		else if (acceptHeader.equals("application/octet-stream")) return ".pdf";
		// default --> Odt
		else return ".odt";
	}
}
