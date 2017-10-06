package fr.insee.rmes.persistance.stamps;

import java.util.TreeSet;
import java.util.stream.IntStream;

public class DefaultStampsImpl implements StampsContract {

	@Override
	public String getStamps() {
		TreeSet<String> stamps = new TreeSet<String>();
		IntStream.range(1, 6).forEach(nbr -> stamps.add("\"Stamp " + nbr + "\""));
		return stamps.toString();
	}

}
