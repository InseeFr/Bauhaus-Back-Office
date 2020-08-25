package fr.insee.rmes.model.operations.documentations;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
import fr.insee.rmes.exceptions.RmesException;
import fr.insee.rmes.model.operations.Indicator;
import fr.insee.rmes.model.operations.Operation;
import fr.insee.rmes.model.operations.Series;

public class ExtensiveSims {
	@Autowired
	static DocumentationsUtils documentationsUtils;
	@Autowired
	static IndicatorsUtils indicatorsUtils;
	@Autowired
	static SeriesUtils seriesUtils;
	
	private Documentation documentation;
	private Series series;
	private Operation operation;
	private Indicator indicator;
	/* un jour créer l'interface Documentable implémentée par Série / Opération / Indicateur ? */ 
	//	private Documentable target;
	
	public ExtensiveSims(Documentation documentation) throws RmesException {

		super();
		this.documentation = documentation;
		String id=documentation.getId();
		
		String[] target = documentationsUtils.getDocumentationTargetTypeAndId(id);
		String targetType = target[0];
		String idDatabase = target[1];

		switch(targetType) {
//		case "OPERATION" : this.operation = indicatorsUtils.getIndicatorById(idDatabase);  break;
//		case "SERIES" : this.series = indicatorsUtils.getIndicatorById(idDatabase);  break;
		case "INDICATOR" : this.indicator = indicatorsUtils.getIndicatorById(idDatabase); break;
		}
		
		documentationsUtils.getDocumentationTargetTypeAndId(id);
	}
	
	
	
	
	
	public Documentation getDocumentation() {
		return documentation;
	}

	public void setDocumentation(Documentation documentation) {
		this.documentation = documentation;
	}
	public Series getSeries() {
		return series;
	}
	public void setSeries(Series series) {
		this.series = series;
	}
	public Operation getOperation() {
		return operation;
	}
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	public Indicator getIndicator() {
		return indicator;
	}
	public void setIndicator(Indicator indicator) {
		this.indicator = indicator;
	}
	
	
	
}
