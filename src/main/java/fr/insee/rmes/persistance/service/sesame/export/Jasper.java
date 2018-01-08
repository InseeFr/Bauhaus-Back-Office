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
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.OutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOdtReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimpleReportExportConfiguration;
import net.sf.jasperreports.export.SimpleRtfReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

public class Jasper {

	final static Logger logger = Logger.getLogger(Jasper.class);

	public InputStream exportConcept(JSONObject json, String acceptHeader) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_concept.jrxml");
		try {
			return export(json, is, acceptHeader);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;
	}

	public InputStream exportCollection(JSONObject json, String acceptHeader) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_collection.jrxml");
		try {
			return export(json, is, acceptHeader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Méthode générique d'export
	 * @param json : données à exporter
	 * @param is : structure du fichier attendu
	 * @param acceptHeader : type de fichier attendu
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static InputStream export(JSONObject json, InputStream is, String acceptHeader) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Map<String, Object> jasperParams = null;
		try {
			jasperParams = mapper.readValue(json.toString(), new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		getJrProperties();// TODO on s'en sert vraiment de ça ???

		JasperDesign jasperReportDesign = JRXmlLoader.load(is);
		JasperReport jasperReport = JasperCompileManager.compileReport(jasperReportDesign);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParams, new JREmptyDataSource());

		Exporter exporter = getExporter(acceptHeader, jasperPrint);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(output);
		exporter.setExporterOutput(exporterOutput);
		exporter.exportReport();

		InputStream in = new ByteArrayInputStream(output.toByteArray());

		return in;
	}

	/**
	 * Construction de l'exporter en fonction du type de sortie attendu
	 * @param acceptHeader mimeType
	 * @param jasperPrint
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Exporter getExporter(String acceptHeader, JasperPrint jasperPrint) {
		Exporter exporter = null;
		SimpleReportExportConfiguration config = null;
		switch (acceptHeader) {
			case "application/octet-stream":// PDF
				exporter = new JRPdfExporter();
				config = new SimplePdfReportConfiguration();
				break;
			case "":// RTF:
				exporter = new JRRtfExporter();
				config = new SimpleRtfReportConfiguration();
				break;
			case "XLS":// XLS
				exporter = new JRXlsExporter();
				config = new SimpleXlsReportConfiguration();
				break;
			case "application/vnd.oasis.opendocument.text":// ODT
			case "Mail":	
				exporter = new JROdtExporter();
				config = new SimpleOdtReportConfiguration();
				break;
			default:// TODO odt pour le moment
				exporter = new JROdtExporter();
				config = new SimpleOdtReportConfiguration();
				break;
		}

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setConfiguration(config);

		return exporter;
	}

	/**
	 * TODO est-ce utile ???
	 */
	private static void getJrProperties() {
		String defaultPDFFont = "Arial";

		JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		JRPropertiesUtil jrPropertiesUtil = JRPropertiesUtil.getInstance(jasperReportsContext);
		jrPropertiesUtil.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		jrPropertiesUtil.setProperty("net.sf.jasperreports.default.font.name", defaultPDFFont);
	}

	/*
		private static InputStream exportOld(JSONObject json, InputStream is, String acceptHeader) throws Exception {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Map<String, Object> jasperParams = null;
			try {
				jasperParams = mapper.readValue(json.toString(), new TypeReference<Map<String, Object>>() {
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			getJrProperties();
	
			JasperDesign jasperReportDesign = JRXmlLoader.load(is);
			JasperReport jasperReport = JasperCompileManager.compileReport(jasperReportDesign);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParams, new JREmptyDataSource());
	
			// Export IS
			// Exporter<ExporterInput, PdfReportConfiguration,
			// PdfExporterConfiguration, OutputStreamExporterOutput> exporter = new
			// JRPdfExporter();
			// exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			// SimplePdfReportConfiguration pdfReportConfiguration = new
			// SimplePdfReportConfiguration();
			// exporter.setConfiguration(pdfReportConfiguration);
	
			Exporter exporter = getExporterOld(acceptHeader, jasperPrint);
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			SimpleReportExportConfiguration config = getReportOld(acceptHeader);
			exporter.setConfiguration(config);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(output);
			exporter.setExporterOutput(exporterOutput);
			exporter.exportReport();
			InputStream in = new ByteArrayInputStream(output.toByteArray());
	
			return in;
		}
	
		@SuppressWarnings("rawtypes")
		private static Exporter getExporterOld(String acceptHeader, JasperPrint jasperPrint) {
			if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
				System.out.println("ok");
				return new JROdtExporter();
			} else if (acceptHeader.equals("application/octet-stream"))
				return new JRPdfExporter();
			// default --> Odt
			else
				return new JROdtExporter();
		}
	
		private static SimpleReportExportConfiguration getReportOld(String acceptHeader) {
			if (acceptHeader.equals("application/vnd.oasis.opendocument.text"))
				return new SimpleOdtReportConfiguration();
			else if (acceptHeader.equals("application/octet-stream"))
				return new SimplePdfReportConfiguration();
			// default --> Odt
			else
				return new SimpleOdtReportConfiguration();
		}
	*/
	public String getExtension(String acceptHeader) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text"))
			return ".odt";
		else if (acceptHeader.equals("application/octet-stream"))
			return ".pdf";
		// default --> Odt
		else
			return ".odt";
	}

}
