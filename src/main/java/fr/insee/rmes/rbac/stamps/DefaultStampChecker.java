package fr.insee.rmes.rbac.stamps;

import java.util.Collections;
import java.util.List;

public class DefaultStampChecker implements  ObjectStampChecker {
    @Override
    public List<String> getStamps() {
        return Collections.emptyList();
    }
}
