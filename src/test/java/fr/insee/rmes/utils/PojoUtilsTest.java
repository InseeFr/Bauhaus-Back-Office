package fr.insee.rmes.utils;

import fr.insee.rmes.model.dataset.PatchDataset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.CachedIntrospectionResults;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PojoUtilsTest {

    @Test
    void testPropertiesNotNull(){
        Object o=null;
        if (o instanceof PatchDataset(
                var updated, var issued, var obs, var time, var start, String temporalCoverageEndDate
        )){

        }
        PatchDataset dataset = new PatchDataset(null, null, 5, null, null, null);
        BeanWrapper beanWrapper = new BeanWrapperImpl(dataset);
        BeanUtil
        Arrays.stream(beanWrapper.getPropertyDescriptors()).filter(propertyDescriptor -> !propertyDescriptor.getName().equals("class"))
                .filter(propertyDescriptor -> propertyDescriptor.)
    }

}