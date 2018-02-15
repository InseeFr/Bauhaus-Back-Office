package fr.insee.rmes.persistance.service.sesame.operations;

import java.util.List;

import fr.insee.rmes.persistance.service.sesame.operations.pojo.SerieForList;

public interface OperationsContract {

	List<SerieForList> getSeries() throws Exception;

}
