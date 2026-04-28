package fr.insee.rmes.modules.ddi.physical_instances.domain.services;


import fr.insee.rmes.modules.ddi.physical_instances.domain.model.CreatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4GroupResponse;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Response;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialCodesList;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialPhysicalInstance;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PhysicalInstanceParents;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.UpdatePhysicalInstanceRequest;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.DDIService;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.serverside.DDIRepository;
import fr.insee.rmes.modules.operation.series.domain.port.serverside.SeriesCreatorsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DDIServiceImpl implements DDIService {
    static final Logger logger = LoggerFactory.getLogger(DDIServiceImpl.class);

    private final DDIRepository ddiRepository;
    private final SeriesCreatorsPort seriesCreatorsPort;

    public DDIServiceImpl(DDIRepository ddiRepository, SeriesCreatorsPort seriesCreatorsPort) {
        this.ddiRepository = ddiRepository;
        this.seriesCreatorsPort = seriesCreatorsPort;
    }

    @Override
    public List<PartialPhysicalInstance> getPhysicalInstances() {
        logger.info("Starting to get physical instances list");
        return ddiRepository.getPhysicalInstances();
    }

    @Override
    public List<PartialCodesList> getCodesLists() {
        logger.info("Starting to get codes lists");
        return ddiRepository.getCodesLists();
    }

    @Override
    public List<PartialGroup> getGroups() {
        logger.info("Starting to get groups list");
        return ddiRepository.getGroups();
    }

    @Override
    public List<PartialGroup> getGroupsFilteredByStamp(Set<String> userStamps) {
        logger.info("Starting to get groups filtered by stamp");
        List<PartialGroup> allGroups = ddiRepository.getGroups();

        Set<String> allSeriesIris = allGroups.stream()
                .flatMap(g -> g.seriesIris().stream())
                .collect(Collectors.toSet());

        Map<String, List<String>> creatorsByIri = seriesCreatorsPort.getCreatorsForSeries(allSeriesIris);

        return allGroups.stream()
                .filter(group -> group.seriesIris().stream()
                        .anyMatch(iri -> {
                            List<String> creators = creatorsByIri.getOrDefault(iri, List.of());
                            return creators.stream().anyMatch(userStamps::contains);
                        }))
                .toList();
    }

    @Override
    public Ddi4GroupResponse getDdi4Group(String agencyId, String id) {
        return ddiRepository.getGroup(agencyId, id);
    }

    @Override
    public Ddi4Response getDdi4PhysicalInstance(String agencyId, String id) {
        return this.ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response updatePhysicalInstance(String agencyId, String id, UpdatePhysicalInstanceRequest request) {
        ddiRepository.updatePhysicalInstance(agencyId, id, request);
        return ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response updateFullPhysicalInstance(String agencyId, String id, Ddi4Response ddi4Response) {
        ddiRepository.updateFullPhysicalInstance(agencyId, id, ddi4Response);
        return ddiRepository.getPhysicalInstance(agencyId, id);
    }

    @Override
    public Ddi4Response createPhysicalInstance(CreatePhysicalInstanceRequest request) {
        logger.info("Creating new physical instance with label: {}", request.physicalInstanceLabel());
        return ddiRepository.createPhysicalInstance(request);
    }

    @Override
    public List<PartialCodesList> getMutualizedCodesLists() {
        logger.info("Starting to get mutualized codes lists");
        return ddiRepository.getMutualizedCodesLists();
    }

    @Override
    public String getItemXml(String agency, String id, String version) {
        logger.info("Getting DDI 3.3 XML for {}/{}/{}", agency, id, version);
        return ddiRepository.getItemXml(agency, id, version);
    }

    @Override
    public String getItemXml(String agency, String id) {
        logger.info("Getting DDI 3.3 XML (latest version) for {}/{}", agency, id);
        return ddiRepository.getItemXml(agency, id);
    }

    @Override
    public PhysicalInstanceParents getPhysicalInstanceParents(String agencyId, String id) {
        logger.info("Getting parents for physical instance {}/{}", agencyId, id);
        return ddiRepository.getPhysicalInstanceParents(agencyId, id);
    }
}