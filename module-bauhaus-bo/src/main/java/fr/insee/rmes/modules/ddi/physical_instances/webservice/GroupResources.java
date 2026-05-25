package fr.insee.rmes.modules.ddi.physical_instances.webservice;

import fr.insee.rmes.modules.commons.configuration.ConditionalOnModule;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.Ddi4Group;
import fr.insee.rmes.modules.ddi.physical_instances.domain.model.PartialGroup;
import fr.insee.rmes.modules.ddi.physical_instances.domain.port.clientside.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for DDI Group operations.
 */
@RestController
@RequestMapping(
        value = "/ddi/groups",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnModule("ddi")
public class GroupResources {

    private static final Logger logger = LoggerFactory.getLogger(GroupResources.class);

    private final GroupService groupService;

    public GroupResources(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public ResponseEntity<List<PartialGroup>> getGroups() {
        logger.info("GET /ddi/groups - Getting all groups");
        try {
            List<PartialGroup> groups = groupService.getAll();
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            logger.error("Failed to get groups", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createOrUpdateGroup(@RequestBody Ddi4Group group) {
        logger.info("POST /ddi/groups - Creating/updating group: id={}", group.id());
        try {
            groupService.createOrUpdate(group);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            logger.error("Failed to create/update group: id={}", group.id(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
