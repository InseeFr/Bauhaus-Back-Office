package fr.insee.rmes.persistance.service.sesame.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.data.JsonDataSource;
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

	final static Logger logger = LogManager.getLogger(Jasper.class);

	public InputStream exportConcept(JSONObject json, String acceptHeader) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_concept.jrxml");
		try {
			return exportSimpleJson(json, is, acceptHeader);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return null;
	}

	public InputStream exportCollection(JSONObject json, String acceptHeader) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_collection.jrxml");
		try {
			return exportSimpleJson(json, is, acceptHeader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public InputStream exportVariableBook(JSONObject json, String acceptHeader) {
		InputStream is = getClass().getClassLoader().getResourceAsStream("jasper/export_varBook.jrxml");
		try {
			return exportCompleteJson(json, is, acceptHeader);
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
	private static InputStream exportSimpleJson(JSONObject json, InputStream is, String acceptHeader) throws Exception {
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
		ByteArrayOutputStream output = exportReport(is, acceptHeader, jasperParams, null);
		InputStream in = new ByteArrayInputStream(output.toByteArray());
		return in;
	}

	private static InputStream exportCompleteJson(JSONObject json, InputStream is, String acceptHeader) throws Exception {
		Map<String, Object> jasperParams = new HashMap<>();
		jasperParams.put("PATH_JASPER",
				"D:\\rco0ck\\Mes Documents\\eclipse_workspace\\Gncs-Back-Office\\src\\main\\resources\\jasper\\");
		InputStream jsonInput = new ByteArrayInputStream(json.toString().getBytes());
		JRDataSource datasource = new JsonDataSource(jsonInput);
		getJrProperties();
		ByteArrayOutputStream output = exportReport(is, acceptHeader, jasperParams, datasource);
		InputStream in = new ByteArrayInputStream(output.toByteArray());
		return in;
	}

	@SuppressWarnings("unchecked")
	private static ByteArrayOutputStream exportReport(InputStream is, String acceptHeader,
			Map<String, Object> jasperParams, JRDataSource dataSource) throws JRException {

		if (dataSource == null) {
			dataSource = new JREmptyDataSource();
		}
		JasperDesign jasperReportDesign = JRXmlLoader.load(is);
		JasperReport jasperReport = JasperCompileManager.compileReport(jasperReportDesign);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, jasperParams, dataSource);

		Exporter exporter = getExporter(acceptHeader, jasperPrint);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		OutputStreamExporterOutput exporterOutput = new SimpleOutputStreamExporterOutput(output);
		exporter.setExporterOutput(exporterOutput);
		exporter.exportReport();
		return output;
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

	private static void getJrProperties() {
		String defaultPDFFont = "Arial";

		JasperReportsContext jasperReportsContext = DefaultJasperReportsContext.getInstance();
		JRPropertiesUtil jrPropertiesUtil = JRPropertiesUtil.getInstance(jasperReportsContext);
		jrPropertiesUtil.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
		jrPropertiesUtil.setProperty("net.sf.jasperreports.default.font.name", defaultPDFFont);
	}

	public String getExtension(String acceptHeader) {
		if (acceptHeader.equals("application/vnd.oasis.opendocument.text")) {
			return ".odt";
		} else if (acceptHeader.equals("application/octet-stream")) {
			return ".pdf";
			// default --> Odt
		} else {
			return ".odt";
		}
	}

}
