package fr.insee.rmes.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateParser {

	 private static List<String> dateFormats ;

	    private static void init(){
	    	if (dateFormats==null||dateFormats.size()==0) {
	    		dateFormats = new ArrayList<String>();
	    		dateFormats.add("yyyy-MM-dd HH:mm:ssxx");
	    		dateFormats.add("yyyy-MM-dd");
	    		dateFormats.add("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	    	}
	    }

	    public static ZonedDateTime parseDate(String dateStr) {
	    	init();
	        for (String format : dateFormats) {
	        	try {
	        		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
	        		ZonedDateTime date = LocalDate.parse(dateStr, formatter).atStartOfDay(ZoneId.systemDefault());
	                return date;
	        	} catch (Exception e) {
	        		e.getMessage();	
	        	}
	        }
	        // All parsers failed
	        return ZonedDateTime.parse(dateStr);
	    }     
	    
	    
	    /**
	     * 
	     * @param String date with format DateTimeFormatter.ISO_DATE_TIME
	     * @return String date with format ISO_LOCAL_DATE
	     */
	    public static String getDate(String dateStr) {
	    	LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
	    	return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
	    }
	
}
