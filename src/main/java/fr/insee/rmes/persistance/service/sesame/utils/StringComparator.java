package fr.insee.rmes.persistance.service.sesame.utils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class StringComparator implements Comparator<String> {

	@Override
	public int compare(String s1, String s2) {
		Collator coll = Collator.getInstance(Locale.FRENCH);
		coll.setStrength(Collator.PRIMARY);
		return coll.compare(s1, s2);
	}

}
