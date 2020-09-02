package fr.insee.rmes.utils;

public final class XhtmlTags {

	public static String OPENLIST = "<ul>";
	public static String CLOSELIST = "</ul>";
	public static String OPENLISTITEM = "<li>";
	public static String CLOSELISTITEM = "</li>";
	
	public static String inListItem (String s) {
		return OPENLISTITEM.concat(s).concat(CLOSELISTITEM);
	}
}
