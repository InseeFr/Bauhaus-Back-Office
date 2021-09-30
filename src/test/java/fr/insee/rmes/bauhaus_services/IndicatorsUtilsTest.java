package fr.insee.rmes.bauhaus_services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import fr.insee.rmes.bauhaus_services.operations.famopeserind_utils.FamOpeSerIndUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.links.OperationsLink;
import fr.insee.rmes.model.operations.Indicator;

class IndicatorsUtilsTest {

	private final static String json = "{\"idSims\":\"1779\",\"wasGeneratedBy\":[{\"labelLg2\":\"Other indexes\",\"labelLg1\":\"Autres indicateurs\",\"id\":\"s1034\"}],\"abstractLg1\":\"Le nombre d'immatriculations de voitures particulières neuves permet de suivre l'évolution du marché automobile français et constitue l'un des indicateurs permettant de calculer la consommation des ménages en automobile.\",\"prefLabelLg1\":\"Immatriculations de voitures particulières neuves\",\"abstractLg2\":\"The number of new private car registrations is used to monitor the trends on the French automobile market and constitutes one of the indicators used to calculate household automobile consumption.\",\"prefLabelLg2\":\"New private car registrations\",\"creators\":[],\"publishers\":[],\"id\":\"p1638\",\"contributors\":[]}  ";
	private final static JSONObject jsonIndicator = new JSONObject(json);
	private Indicator indicator;
	

    
    @InjectMocks //CLASS TO TEST
    private IndicatorsUtils indicatorsUtils;
    
    
   // @Mock
 	@InjectMocks
    FamOpeSerIndUtils famOpeSerIndUtilsMock;
 	
	
    @BeforeEach
    public void init() {
    	famOpeSerIndUtilsMock = Mockito.spy(new FamOpeSerIndUtils());
        MockitoAnnotations.initMocks(this);
        
        try {
			initIndicator();
		} catch (RmesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    //getCodeListByNotation//

    @Test
    void givenBuildIndicatorFromJson_whenCorrectRequest_thenResponseIsOk() throws RmesException {
    	
        Indicator indicatorByApp = indicatorsUtils.buildIndicatorFromJson(jsonIndicator);
        Assertions.assertEquals(indicator, indicatorByApp);

    }
    
    	 
        public void initIndicator() throws RmesException {
    		indicator = new Indicator();
    		indicator.setId("p1638");
    		indicator.setIdSims("1779");

    		indicator.setAbstractLg1("Le nombre d'immatriculations de voitures particulières neuves permet de suivre l'évolution du marché automobile français et constitue l'un des indicateurs permettant de calculer la consommation des ménages en automobile.");
    		indicator.setAbstractLg2("The number of new private car registrations is used to monitor the trends on the French automobile market and constitutes one of the indicators used to calculate household automobile consumption.");
    		indicator.setPrefLabelLg1("Immatriculations de voitures particulières neuves");
    		indicator.setPrefLabelLg2("New private car registrations");
    		
    		List<String> creators = new ArrayList<>();
    		indicator.setCreators(creators);
        		
    		List<OperationsLink> pubList = new ArrayList<>();
    		indicator.setPublishers(pubList);
    		
    		List<OperationsLink> contrList = new ArrayList<>();
    		indicator.setContributors(contrList);
    		
    		OperationsLink wgb = new OperationsLink("s1034",null,"Autres indicateurs","Other indexes");
    		List<OperationsLink> wgbList = new ArrayList<>();
    		wgbList.add(wgb);
    		indicator.setWasGeneratedBy(wgbList);
    	
    	}


}
