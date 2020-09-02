package fr.insee.rmes.utils;

public final class XhtmlTags {

	public static String OPENLIST = "<ul>";
	public static String CLOSELIST = "</ul>";
	public static String OPENLISTITEM = "<li>";
	public static String CLOSELISTITEM = "</li>";
	public static String EMPTYPARAGRAPH = "<p></p>";
	public static String OPENUPPERCASE = "<U>";
	public static String CLOSEUPPERCASE = "</U>";
	
	public static String inListItem (String s) {
		return OPENLISTITEM.concat(s).concat(CLOSELISTITEM);
	}
	public static String inUpperCase (String s) {
		return OPENUPPERCASE.concat(s).concat(CLOSEUPPERCASE);
	}
}
