package fr.insee.rmes.utils;

import fr.insee.rmes.config.Config;
import fr.insee.rmes.stubs.ConfigStub;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

	private final Config config = new ConfigStub();
	private final FilesUtils filesUtils = new FilesUtils(config);

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
		assertEquals("name-name", filesUtils.reduceFileNameSize("name-name"));
	}

	@Test
	void shouldSubStringIfLongName(){
		assertEquals("a".repeat(ConfigStub.FILE_MAX_LENGTH), filesUtils.reduceFileNameSize("a".repeat(ConfigStub.FILE_MAX_LENGTH+1)));
	}
}
