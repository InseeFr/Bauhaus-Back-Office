package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilsTest {

	@Test
	void should_return_date_if_good_format() {
		String date = DateUtils.getDate("2018-12-17");
		assertEquals("2018-12-17", date);
	}

	@Test
	void should_return_date_if_bad_format() {
		String date = DateUtils.getDate("12-17");
		assertEquals("12-17", date);
	}

}
