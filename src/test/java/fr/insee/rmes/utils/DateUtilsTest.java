package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilsTest {

	@Test
	void givenDate_whenGetDate_thenResponseIsClean() throws RmesException {

		String date = DateUtils.getDate("2018-12-17");
		assertEquals("2018-12-17", date);
	}

}
