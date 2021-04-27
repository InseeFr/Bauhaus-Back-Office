package fr.insee.rmes.model.operations.documentations;

import org.springframework.beans.factory.annotation.Autowired;

import fr.insee.rmes.bauhaus_services.operations.documentations.DocumentationsUtils;
import fr.insee.rmes.bauhaus_services.operations.indicators.IndicatorsUtils;
import fr.insee.rmes.bauhaus_services.operations.series.SeriesUtils;
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
