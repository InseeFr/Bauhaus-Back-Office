package fr.insee.rmes.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fr.insee.rmes.exceptions.RmesException;

class DateUtilsTest {

	@Test
	void givenDate_whenGetDate_thenResponseIsClean() throws RmesException {

		String date = DateUtils.getDate("2018-12-17");
		assertEquals("2018-12-17", date);
	}

}
