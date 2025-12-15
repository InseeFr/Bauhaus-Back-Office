package fr.insee.rmes.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateUtilsTest {

	@Test
	void getCurrentDate_ShouldReturnNonNullValue() {
		String currentDate = DateUtils.getCurrentDate();
		Assertions.assertNotNull(currentDate);
	}

	@Test
	void parseDateTime_ShouldParseValidZonedDateTime() {
		String dateStr = "2024-12-09T12:30:45+01:00";
		LocalDateTime result = DateUtils.parseDateTime(dateStr);
		assertEquals(LocalDateTime.of(2024, 12, 9, 12, 30, 45), result);
	}

	@Test
	void parseDateTime_ShouldParseValidDateWithoutTime() {
		String dateStr = "2024-12-09";
		LocalDateTime result = DateUtils.parseDateTime(dateStr);
		assertEquals(LocalDateTime.of(2024, 12, 9, 0, 0), result);
	}

	@Test
	void parseDateTime_ShouldReturnNullForInvalidDate() {
		String invalidDate = "invalid-date";
		assertThrows(Exception.class, () -> DateUtils.parseDateTime(invalidDate));
	}

	@Test
	void getDate_ShouldConvertToISODateFormat() {
		String isoDateTime = "2024-12-09T15:45:30";
		String result = DateUtils.getDate(isoDateTime);
		assertEquals("2024-12-09", result);
	}

	@Test
	void getDate_ShouldReturnOriginalForInvalidDate() {
		String invalidDate = "invalid-date";
		String result = DateUtils.getDate(invalidDate);
		assertEquals(invalidDate, result);
	}

	@Test
	void toDate_ShouldFormatToDDMMYYYY() {
		String dateTime = "2024-12-09T15:45:30";
		String result = DateUtils.toDate(dateTime);
		assertEquals("09/12/2024", result);
	}

	@Test
	void toDate_ShouldReturnOriginalStringForShortString() {
		String shortString = "2024";
		String result = DateUtils.toDate(shortString);
		assertEquals(shortString, result);
	}
}
