package fr.insee.rmes.model.rbac;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ModuleAccessPrivileges(EnumMap<Privilege, Strategy> strategysByPrivileges) {

    public ModuleAccessPrivileges{
        Objects.requireNonNull(strategysByPrivileges);
    }

    public static final ModuleAccessPrivileges NO_PRIVILEGE = new ModuleAccessPrivileges(new EnumMap<>(Privilege.class));

    public ModuleAccessPrivileges(Map<Privilege, Strategy> strategysByPrivileges){
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
        EnumMap<Privilege, Strategy> mergedPrivileges = new EnumMap<>(Privilege.class);
        for (Privilege privilege : Privilege.values()) {
            mergedPrivileges.put(privilege, Strategy.merge(strategysByPrivileges.get(privilege), other.strategysByPrivileges.get(privilege)));
        }
        return new ModuleAccessPrivileges(mergedPrivileges);
    }

    public Optional<Strategy> strategyFor(Privilege privilege) {
        return Optional.ofNullable(strategysByPrivileges.get(privilege));
    }
}
