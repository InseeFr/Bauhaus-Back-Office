package fr.insee.rmes.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import fr.insee.rmes.bauhaus_services.Constants;
import fr.insee.rmes.exceptions.RmesException;

class XMLUtilsTest {
	
	String series = "{\r\n"
			+ "	\"prefLabelLg1\": \"Base permanente des équipements\",\r\n"
			+ "	\"prefLabelLg2\": \"Permanent database of facilities\",\r\n"
			+ "	\"replaces\": [\r\n"
			+ "		{\r\n"
			+ "			\"labelLg2\": \"Municipality Inventory\",\r\n"
			+ "			\"labelLg1\": \"Inventaire communal\",\r\n"
			+ "			\"id\": \"s1248\",\r\n"
			+ "			\"type\": \"series\"\r\n"
			+ "		}\r\n"
			+ "	],\r\n"
			+ "	\"dataCollectors\": [],\r\n"
			+ "	\"creators\": [\r\n"
			+ "		\"DR86-SES87\"\r\n"
			+ "	],\r\n"
			+ "	\"seeAlso\": [],\r\n"
			+ "	\"typeCode\": \"A\",\r\n"
			+ "	\"operations\": [\r\n"
			+ "		{\r\n"
			+ "			\"labelLg2\": \"Permanent database of facilities 2018\",\r\n"
			+ "			\"labelLg1\": \"Base permanente des équipements 2018\",\r\n"
			+ "			\"id\": \"s1551\"\r\n"
			+ "		},\r\n"
			+ "		{\r\n"
			+ "			\"labelLg2\": \"BPE 2020\",\r\n"
			+ "			\"labelLg1\": \"Base permanente 2020\",\r\n"
			+ "			\"id\": \"s1579\"\r\n"
			+ "		}\r\n"
			+ "	],\r\n"
			+ "	\"abstractLg1\": \"<ul>\\n<li><strong>gras</strong></li>\\n<li><em>italique</em></li>\\n<li><strong><em>gras et italique</em></strong></li>\\n<li><strong><em>puces</em></strong></li>\\n<li><strong><em>puce2</em></strong></li>\\n<li><strong><em>puces</em></strong></li>\\n</ul>\",\r\n"
			+ "	\"typeList\": \"CL_SOURCE_CATEGORY\",\r\n"
			+ "	\"abstractLg2\": \"<ol>\\n<li>The permanent database of facilities (BPE) is a statistical database. It lists a wide range of equipment and services, merchant or not, accessible to the public throughout France on January, 1st each year.It covers more than 180 different types of services and facilities, divided into seven main groups : personal services, businesses, education, health and social services, transport-travel, sports-leisure-culture and tourism. </li>\\n<li>\\n<p>blabla suite de la puce</p>\\n</li>\\n<li>\\n<p>BPE is built from various administrative sources. It collects data on access points to services intended for the population, or &quot;equipment&quot;, located at fine geographical levels : municipalities, sub-municipal territories (Iris) and coordinates (x,y) for most types of equipment.</p>\\n</li>\\n<li>blabla suite de la puce</li>\\n</ol>\\n<p>By the detailed knowledge of the territories it allows, BPE is a decision-making aid tool. IIt makes it possible in particular to study the structure of the supply of services in a territory: volume of equipment, presence or absence, concentration or dispersion, identification of service centers or territories without services, calculation of distances between municipalities equipped and not equipped, calculation of equipment rates by bringing together equipment and its potential users, setting up of equipment baskets on a particular topic, etc.</p>\\n<p>The BPE also offers a grid for reading the territories by grouping certain types of equipment into sets called : the ranges bring together equipment that present similar logics of implantation, in the sense that they are frequently present in the same municipalities. These groupings make it possible to develop synthetic indicators reflecting the hierarchical organization of the territories in terms of services to the population.</p>\\n<p>Since September 2018, data on BPE have been released in evolution throughout the territory, excluding Mayotte. They cover a limited number of types of facilities and two years spaced five years apart : 2012-2017, 2013-2018, etc.</p>\",\r\n"
			+ "	\"historyNoteLg2\": \"<p>The permanent base of equipment replaces the municipal inventory, the last of which was carried out in 1998. Its first diffusion concerns data from 2008. These are updated each year, instead of every 7 or 8 years with the municipal inventory. The BPE lists moreover a greater variety of equipment (188 in 2019 instead of 36) ; their location is also more accurate, in Iris or even in coordinates (x, y).It covers the Metropolitan France, the four &quot;historic&quot; overseas departments and, since 2012, the department of Mayotte.Since 2018, an evolution database on a five-year period has been uploaded each year, it relates to a limited number of types of equipment, determined each year following a valuation.</p>\",\r\n"
			+ "	\"historyNoteLg1\": \"<ol>\\n<li>La <strong>base permanente</strong> des équipements se substitue à l'inventaire communal, dont le dernier a été réalisé en 1998. Sa première diffusion porte sur des données de 2008. Celles-ci sont actualisées chaque année, au lieu de tous les 7 ou 8 ans pour l'inventaire communal. De plus, la BPE répertorie une plus grande variété d'équipements (188 en 2019 au lieu de 36) ; leur localisation est également plus affinée, à l'Iris voire en coordonnées (x,y).</li>\\n<li>Elle couvre le <em>territoire métropolitain</em>, les quatre départements d'outre-mer &lt;&lt; historiques &gt;&gt;  et, depuis 2012, le département de Mayotte.</li>\\n</ol>\\n<p>Depuis 2018, une base en évolution sur un pas quinquennal est diffusée chaque année, elle porte sur un nombre restreint de types d'équipements, déterminés chaque année suite à une expertise.</p>\",\r\n"
			+ "	\"publishers\": [\r\n"
			+ "		{\r\n"
			+ "			\"labelLg1\": \"Direction des statistiques démographiques et sociales (DSDS)\",\r\n"
			+ "			\"id\": \"DG75-F001\"\r\n"
			+ "		}\r\n"
			+ "	],\r\n"
			+ "	\"altLabelLg2\": \"BPE\",\r\n"
			+ "	\"id\": \"s1161\",\r\n"
			+ "	\"contributors\": [],\r\n"
			+ "	\"altLabelLg1\": \"BPE\",\r\n"
			+ "	\"family\": {\r\n"
			+ "		\"labelLg2\": \"Facilities census\",\r\n"
			+ "		\"labelLg1\": \"Recensement des équipements\",\r\n"
			+ "		\"id\": \"s1\"\r\n"
			+ "	},\r\n"
			+ "	\"isReplacedBy\": [],\r\n"
			+ "	\"validationState\": \"Modified\"\r\n"
			+ "}";
	@Test
	void givenJSon_whenSolveXml_thenResponseIsClean() throws RmesException {

		String out = XMLUtils.solveSpecialXmlcharacters(series);
		assertTrue(out.contains(Constants.XML_ESPERLUETTE_REPLACEMENT + "quot;"));
	}

}
