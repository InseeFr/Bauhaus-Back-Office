package fr.insee.rmes.utils;

import com.vladsch.flexmark.convert.html.FlexmarkHtmlParser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataSet;

public class XhtmlToMarkdownUtils {
	
	public static String xhtmlToMarkdown(String xhtml) {
		String md = FlexmarkHtmlParser.parse(xhtml);
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

}
