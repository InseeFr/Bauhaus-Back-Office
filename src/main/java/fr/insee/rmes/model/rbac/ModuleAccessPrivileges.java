package fr.insee.rmes.model.rbac;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public record ModuleAccessPrivileges(EnumMap<RBAC.Privilege, RBAC.Strategy> strategysByPrivileges) {

    public static final ModuleAccessPrivileges NO_PRIVILEGE = new ModuleAccessPrivileges(new EnumMap<>(RBAC.Privilege.class));

    public ModuleAccessPrivileges(Map<RBAC.Privilege, RBAC.Strategy> strategysByPrivileges){
        this(new EnumMap<>(strategysByPrivileges));
    }

    public static ModuleAccessPrivileges merge(ModuleAccessPrivileges moduleAccessPrivileges1, ModuleAccessPrivileges moduleAccessPrivileges2) {
        if(moduleAccessPrivileges1!=null){
            return moduleAccessPrivileges1.merge(moduleAccessPrivileges2);
        }
        return moduleAccessPrivileges2;
    }

    private ModuleAccessPrivileges merge(ModuleAccessPrivileges other) {
        if (other == null){
            return this;
        }
        EnumMap<RBAC.Privilege, RBAC.Strategy> mergedPrivileges = new EnumMap<>(RBAC.Privilege.class);
        for (RBAC.Privilege privilege : RBAC.Privilege.values()) {
            mergedPrivileges.put(privilege, RBAC.Strategy.merge(strategysByPrivileges.get(privilege), other.strategysByPrivileges.get(privilege)));
        }
        return new ModuleAccessPrivileges(mergedPrivileges);
    }

    public Optional<RBAC.Strategy> strategyFor(RBAC.Privilege privilege) {
        return Optional.ofNullable(strategysByPrivileges.get(privilege));
    }
}
