package fr.insee.rmes.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.insee.rmes.exceptions.RmesException;

class FileUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = { "Carrières complètes ", "carrières-complètes", "  Carrières    complètes  " })
	void givenCleanFileName_whenString_thenResponseIsClean(String name) throws RmesException {

		String cleanFileName = FileUtils.cleanFileNameAndAddExtension(name, "odt");
		assertEquals("carrières-complètes.odt", cleanFileName);
	}

	@Test
	void givenCleanFileName_whenStringWithPointExtension_thenResponseIsClean() throws RmesException {

		String cleanFileName = FileUtils.cleanFileNameAndAddExtension("test de nommage bidon ", ".odt");
		assertEquals("test-de-nommage-bidon.odt", cleanFileName);
	}

}
