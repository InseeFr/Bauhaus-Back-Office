package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilesUtilsTest {

	@Test
	void testGenerateFinalFileNameWithExtension() {
		String fileName = "example_file_œ.txt";
		int maxLength = 10;

		String result = FilesUtils.generateFinalFileNameWithExtension(fileName, maxLength);

		assertEquals("exampleFil.txt", result, "Le nom de fichier généré avec extension est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithExtension_LongFileName() {
		String fileName = "averylongfilenameexample.txt";
		int maxLength = 15;

		String result = FilesUtils.generateFinalFileNameWithExtension(fileName, maxLength);

		assertEquals("averylongfilena.txt", result, "Le nom de fichier généré avec extension pour un long nom est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithoutExtension() {
		String fileName = "example_file_œ.txt";
		int maxLength = 10;

		String result = FilesUtils.generateFinalFileNameWithoutExtension(fileName, maxLength);

		assertEquals("exampleFil", result, "Le nom de fichier généré sans extension est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithoutExtension_LongFileName() {
		String fileName = "averylongfilenameexample";
		int maxLength = 20;

		String result = FilesUtils.generateFinalFileNameWithoutExtension(fileName, maxLength);

		assertEquals("averylongfilenameexa", result, "Le nom de fichier généré sans extension pour un long nom est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithExtension_SpecialCharacters() {
		String fileName = "fi!le&na:me?.txt";
		int maxLength = 10;

		String result = FilesUtils.generateFinalFileNameWithExtension(fileName, maxLength);

		assertEquals("filename.txt", result, "Le nom de fichier généré avec des caractères spéciaux est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithoutExtension_SpecialCharacters() {
		String fileName = "fi!le&na:me?";
		int maxLength = 10;

		String result = FilesUtils.generateFinalFileNameWithoutExtension(fileName, maxLength);

		assertEquals("filename", result, "Le nom de fichier sans extension avec des caractères spéciaux est incorrect");
	}

	@Test
	void testGetMediaTypeFromExtension_validExtensions() {
		// Vérifie les extensions valides
		assertEquals(new MediaType("application", "vnd.oasis.opendocument.text"),
				FilesUtils.getMediaTypeFromExtension(FilesUtils.ODT_EXTENSION));

		assertEquals(new MediaType("application", "vnd.oasis.opendocument.spreadsheet"),
				FilesUtils.getMediaTypeFromExtension(FilesUtils.ODS_EXTENSION));

		assertEquals(new MediaType("application", "zip"),
				FilesUtils.getMediaTypeFromExtension(FilesUtils.ZIP_EXTENSION));
	}

	@Test
	void testGetMediaTypeFromExtension_invalidExtension() {
		// Vérifie que les extensions invalides génèrent une exception
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			FilesUtils.getMediaTypeFromExtension("invalid_extension");
		});

		assertEquals("Unexpected value: invalid_extension", exception.getMessage());
	}

	@Test
	void testGetExtension() {
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
}
