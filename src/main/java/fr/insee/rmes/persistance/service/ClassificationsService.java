package fr.insee.rmes.persistance.service;

public interface ClassificationsService {
	
	public String getFamilies() throws Exception;
	
	public String getFamily(String id) throws Exception;
	
	public String getFamilyMembers(String id) throws Exception;

	public String getSeries() throws Exception;
	
	public String getOneSeries(String id) throws Exception;
	
	public String getSeriesMembers(String id) throws Exception;
	
	public String getClassifications() throws Exception;

}
