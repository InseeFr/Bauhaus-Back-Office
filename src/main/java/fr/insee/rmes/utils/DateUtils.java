package fr.insee.rmes.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DateUtils {

    private static final Logger logger = LogManager.getLogger(DateUtils.class);

    private static List<String> dateFormats;
    
	  private DateUtils() {
		    throw new IllegalStateException("Utility class");
	}


    private static void init() {
        if (dateFormats == null || dateFormats.isEmpty()) {
            dateFormats = new ArrayList<>();
            dateFormats.add("yyyy-MM-dd HH:mm:ssxx");
            dateFormats.add("yyyy-MM-dd");
            dateFormats.add("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormats.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
        }
    }

    public static String getCurrentDate() {
        return LocalDateTime.now().toString();
    }

    public static LocalDateTime parseDateTime(String dateStr) {
        init();
        try {
            return ZonedDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
        for (String format : dateFormats) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            try {//keep the time
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
            try {//time is set to 00:00.000
                return LocalDate.parse(dateStr, formatter).atStartOfDay();
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
        // All parsers failed
        return LocalDateTime.parse(dateStr);
    }

    public static Date parseDate(String dateStr) {
        return convertDateTimeToDate(parseDateTime(dateStr));
    }

    private static Date convertDateTimeToDate(LocalDateTime dateTime) {
        return Date
                .from(dateTime.atZone(ZoneId.systemDefault())
                        .toInstant());
    }


    /**
     * @param String date with format DateTimeFormatter.ISO_DATE_TIME
     * @return String date with format ISO_LOCAL_DATE
     */
    public static String getDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            logger.debug("Date can't be parse in DateTime : {} {} {}" , dateStr, e.getMessage(), e.getClass().getSimpleName());
            return dateStr;
        }
    }

}
