package fr.insee.rmes.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class XhtmlToMarkdownUtils {
	
	private static MutableDataSet optionsXhtmlToMd;
	
	private static void init(){
		if (optionsXhtmlToMd==null || optionsXhtmlToMd.getKeys().isEmpty()) {
			optionsXhtmlToMd = new MutableDataSet();
			optionsXhtmlToMd.set(FlexmarkHtmlConverter.SKIP_CHAR_ESCAPE,true);
		}
	}
	
	private static String xhtmlToMarkdown(String xhtml) {
		init();
      	String md = FlexmarkHtmlConverter.builder(optionsXhtmlToMd).build().convert(xhtml);
		if (md.endsWith("\n")){
			return md.substring(0,md.length()-1);
		}
		return md;
	}
	
    public static String markdownToXhtml(String md) {
    	if (StringUtils.isEmpty(md)) { return md;}
        MutableDataSet options = new MutableDataSet();
        options.set(FlexmarkHtmlConverter.LISTS_END_ON_DOUBLE_BLANK,true);
        options.setFrom(ParserEmulationProfile.MARKDOWN);
        
        //convert soft-breaks to hard breaks
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(md);
        return renderer.render(document); 
    }
    
    private XhtmlToMarkdownUtils() {
    	throw new IllegalStateException("Utility class");
    }
    
	public static void convertJSONObject(JSONObject jsonObj) {
		jsonObj.keySet().forEach(keyStr ->
		{
			Object keyvalue = jsonObj.get(keyStr);
			if (keyvalue instanceof JSONObject  ) {
				convertJSONObject((JSONObject)keyvalue);
			} else if (keyvalue instanceof JSONArray ) {
				convertJSONArray((JSONArray)keyvalue);
			} else {
				jsonObj.put(keyStr, XhtmlToMarkdownUtils.xhtmlToMarkdown((String) keyvalue));
			}
		});
	}

	public static void convertJSONArray(JSONArray jsonArr) {
		for (int i = 0; i < jsonArr.length(); i++) {
			if (jsonArr.get(i) instanceof JSONObject) {
				convertJSONObject(jsonArr.getJSONObject(i));
			}
		}
	}

}
