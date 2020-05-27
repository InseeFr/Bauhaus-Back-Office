package fr.insee.rmes.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class XhtmlToMarkdownUtils {
	
	static MutableDataSet optionsXhtmlToMd;
	
	private static void init(){
		if (optionsXhtmlToMd==null || optionsXhtmlToMd.getKeys().size()==0) {
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
        MutableDataSet options = new MutableDataSet();

        // uncomment to set optional extensions
        //options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

        // uncomment to convert soft-breaks to hard breaks
     //   options.set(HtmlRenderer.SOFT_BREAK, "<br />");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parse(md);
        String xhtml = renderer.render(document); 
        return xhtml;
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
