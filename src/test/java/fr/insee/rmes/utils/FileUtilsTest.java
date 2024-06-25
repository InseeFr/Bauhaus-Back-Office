package fr.insee.rmes.utils;

import fr.insee.rmes.exceptions.RmesException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

	@Test
	public void testGetExtension() {
		// Test with null input
		assertEquals(".odt", FilesUtils.getExtension(null));

		// Test with "application/octet-stream"
		assertEquals(".pdf", FilesUtils.getExtension("application/octet-stream"));

		// Test with "flatODT"
		assertEquals(".fodt", FilesUtils.getExtension("flatODT"));

		// Test with "XML"
		assertEquals(".xml", FilesUtils.getExtension("XML"));

		// Test with "application/vnd.oasis.opendocument.text"
		assertEquals(".odt", FilesUtils.getExtension("application/vnd.oasis.opendocument.text"));

		// Test with unknown input
		assertEquals(".odt", FilesUtils.getExtension("unknown/type"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "Carrières complètes ", "carrières-complètes", "  Carrières    complètes  " })
	void givenCleanFileName_whenString_thenResponseIsClean(String name) throws RmesException {

		String cleanFileName = FilesUtils.cleanFileNameAndAddExtension(name, "odt");
		assertEquals("carrières-complètes.odt", cleanFileName);
	}

	@Test
	void givenCleanFileName_whenStringWithPointExtension_thenResponseIsClean() throws RmesException {

		String cleanFileName = FilesUtils.cleanFileNameAndAddExtension("test de nommage bidon ", ".odt");
		assertEquals("test-de-nommage-bidon.odt", cleanFileName);
	}

}
