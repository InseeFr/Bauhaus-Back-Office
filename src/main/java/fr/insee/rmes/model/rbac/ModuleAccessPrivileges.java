package fr.insee.rmes.model.rbac;

import java.util.EnumMap;
import java.util.Optional;

public record ModuleAccessPrivileges(EnumMap<RBAC.Privilege, RBAC.Strategy> strategysByPrivileges) {

    public static Optional<ModuleAccessPrivileges> merge(ModuleAccessPrivileges moduleAccessPrivileges1, ModuleAccessPrivileges moduleAccessPrivileges2) {
        if(moduleAccessPrivileges1!=null){
            return Optional.of(moduleAccessPrivileges1.merge(moduleAccessPrivileges2));
        }
        if(moduleAccessPrivileges2!=null){
            return Optional.of(moduleAccessPrivileges2.merge(moduleAccessPrivileges1));
        }
        return Optional.empty();
    }

    private ModuleAccessPrivileges merge(ModuleAccessPrivileges other) {
        if (other == null){
            return this;
        }
        EnumMap<RBAC.Privilege, RBAC.Strategy> mergedPrivileges = new EnumMap<>(RBAC.Privilege.class);
        for (RBAC.Privilege privilege : RBAC.Privilege.values()) {
            RBAC.Strategy.merge(strategysByPrivileges.get(privilege), other.strategysByPrivileges.get(privilege))
                    .ifPresent(s->mergedPrivileges.put(privilege, s));
        }
        return new ModuleAccessPrivileges(mergedPrivileges);
    }


}
