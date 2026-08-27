package fr.insee.rmes.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesUtilsTest {

	@Test
	void testGenerateFinalFileNameWithExtension() {
		String fileName = "example_file_œ’.txt";
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
		String fileName = "example_file_œ’.txt";
		int maxLength = 10;

		String result = FilesUtils.generateFinalFileNameWithoutExtension(fileName, maxLength);

		assertEquals("exampleFil", result, "Le nom de fichier généré sans extension est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithoutExtension_LongFileName() {
		String fileName = "averylongfilenameexample";
		int maxLength = 20;

		String result = FilesUtils.generateFinalFileNameWithoutExtension(fileName, maxLength);

		assertEquals("rapportQualiteEnquet", FilesUtils.generateFinalFileNameWithoutExtension("Rapport qualité : Enquête annuelle de recensement 2022", maxLength), "Le nom de fichier généré sans extension pour un long nom est incorrect");
		assertEquals("averylongfilenameexa", result, "Le nom de fichier généré sans extension pour un long nom est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithExtension_SpecialCharacters() {
		String fileName = "fi!le&na:me?’.txt";
		int maxLength = 10;

		String result = FilesUtils.generateFinalFileNameWithExtension(fileName, maxLength);

		assertEquals("filename.txt", result, "Le nom de fichier généré avec des caractères spéciaux est incorrect");
	}

	@Test
	void testGenerateFinalFileNameWithoutExtension_SpecialCharacters() {
		String fileName = "fi!le&na:me?’";
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

	@Test
	void generate_final_file_name_should_remove_every_diacritic() {
		assertEquals("eleveAeiouCnu", FilesUtils.generateFinalFileNameWithoutExtension("Élève àéîôû çñü", 50));
	}

	@Test
	void generate_final_file_name_should_transliterate_letters_that_have_no_diacritic_to_strip() {
		assertEquals("coeurAequoOstStrasse", FilesUtils.generateFinalFileNameWithoutExtension("Cœur æquo Øst Straße", 50));
	}

	@Test
	void generate_final_file_name_should_remove_typographic_punctuation() {
		assertEquals("enqueteEmploi2022N1", FilesUtils.generateFinalFileNameWithoutExtension("Enquête « Emploi » – 2022 … n°1", 50));
	}

	@Test
	void generate_final_file_name_should_treat_a_non_breaking_space_as_a_word_separator() {
		assertEquals("statistiques2024", FilesUtils.generateFinalFileNameWithoutExtension("Statistiques\u00A02024", 50));
	}

	@Test
	void generate_final_file_name_should_only_keep_ascii_letters_and_digits() {
		String result = FilesUtils.generateFinalFileNameWithoutExtension("Отчёт λ 2024 — €100 %", 50);

		assertTrue(result.matches("[A-Za-z0-9]*"), "Le nom de fichier généré doit être purement ASCII, obtenu : " + result);
	}

	@Test
	void generate_final_file_name_with_extension_should_clean_the_extension_too() {
		assertEquals("rapportEte.pdf", FilesUtils.generateFinalFileNameWithExtension("Rapport été.pdf", 50));
	}

	@Test
	void generate_final_file_name_with_extension_should_not_append_a_dot_when_the_name_has_no_extension() {
		assertEquals("rapportEte", FilesUtils.generateFinalFileNameWithExtension("Rapport été", 50));
	}


	@Test
	void generate_final_file_name_should_fall_back_on_a_default_name_when_nothing_ascii_remains() {
		assertEquals("export", FilesUtils.generateFinalFileNameWithoutExtension("«»…", 50));
		assertEquals("export.pdf", FilesUtils.generateFinalFileNameWithExtension("«»….pdf", 50));
	}

}
