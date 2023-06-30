package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = { "Carrières complètes ", "carrières-complètes", "  Carrières    complètes  " })
	void givenCleanFileName_whenString_thenResponseIsClean(String name) {

		String cleanFileName = FilesUtils.cleanFileNameAndAddExtension(name, "odt");
		assertEquals("carrières-complètes.odt", cleanFileName);
	}

	@Test
	void givenCleanFileName_whenStringWithPointExtension_thenResponseIsClean() {

		String cleanFileName = FilesUtils.cleanFileNameAndAddExtension("test de nommage bidon ", ".odt");
		assertEquals("test-de-nommage-bidon.odt", cleanFileName);
	}

	@Test
	void shouldNotSubStringIfShortName(){
		assertEquals("name-name", FilesUtils.reduceFileNameSize("name-name"));
	}

	@Test
	void shouldSubStringIfLongName(){
		assertEquals("a".repeat(200), FilesUtils.reduceFileNameSize("a".repeat(300)));
	}
}
