package fr.insee.rmes.utils;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;

public class PojoUtils {

    private PojoUtils() {}

    public static void copyPropertiesNonNull(@NotNull Object source, @NotNull Object target) {
        BeanUtils.copyProperties(source, target, ) ;
    }

}
