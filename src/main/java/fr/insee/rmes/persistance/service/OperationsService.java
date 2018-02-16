package fr.insee.rmes.persistance.service;

import java.util.List;

import fr.insee.rmes.persistance.service.sesame.operations.series.SerieForList;

public interface OperationsService {

	List<SerieForList> getSeries() throws Exception;

}
