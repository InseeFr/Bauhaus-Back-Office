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
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.PdfExporterConfiguration;
import net.sf.jasperreports.export.PdfReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;

public class Jasper {
	
	final static Logger logger = Logger.getLogger(Jasper.class);
	
	public InputStream exportConcept(JSONObject json)  {

		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_concept.jrxml");
		try {
			return export(json, is);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;
	}

	public InputStream exportCollection(JSONObject json)  {

		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_collection.jrxml");
		try {
			return export(json, is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static InputStream export(JSONObject json, InputStream is) throws Exception {
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
		Exporter<ExporterInput, PdfReportConfiguration, PdfExporterConfiguration, OutputStreamExporterOutput> exporter = new JRPdfExporter();
		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		SimplePdfReportConfiguration pdfReportConfiguration = new SimplePdfReportConfiguration();
		exporter.setConfiguration(pdfReportConfiguration);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(output);
		exporter.setExporterOutput(exporterOutput);
		exporter.exportReport();
		InputStream in = new ByteArrayInputStream(output.toByteArray());
		return in;		

	}
}
