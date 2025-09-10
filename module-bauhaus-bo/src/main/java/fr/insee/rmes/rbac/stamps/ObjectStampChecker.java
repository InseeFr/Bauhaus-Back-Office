package fr.insee.rmes.rbac.stamps;

import java.util.List;

public interface ObjectStampChecker {

    List<String> getStamps(String id);
}
