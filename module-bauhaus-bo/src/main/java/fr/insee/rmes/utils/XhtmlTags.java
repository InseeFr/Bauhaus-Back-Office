package fr.insee.rmes.utils;

public final class XhtmlTags {

	  private XhtmlTags() {
		    throw new IllegalStateException("Utility class");
	}

	
	public static final String OPENLIST = "<ul>";
	public static final String CLOSELIST = "</ul>";
	public static final String OPENLISTITEM = "<li>";
	public static final String CLOSELISTITEM = "</li>";
	public static final String PARAGRAPH = "<p></p>";
	public static final String OPENUPPERCASE = "<U>";
	public static final String CLOSEUPPERCASE = "</U>";
	
	public static final String inListItem (String s) {
		return OPENLISTITEM.concat(s).concat(CLOSELISTITEM);
	}
	public static final String inUpperCase (String s) {
		return OPENUPPERCASE.concat(s).concat(CLOSEUPPERCASE);
	}
}
