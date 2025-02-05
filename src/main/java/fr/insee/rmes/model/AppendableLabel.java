package fr.insee.rmes.model;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public interface AppendableLabel<R extends AppendableLabel<R>> {

    default R appendLabel(R other){
        return withAltLabels(this,this.altLabels() + " || " + other.altLabels());
    }

    String altLabels();

    private R withAltLabels(AppendableLabel<R> instance, String newAltLabel){
        var classR = instance.getClass();
        Method withAltLabels = ReflectionUtils.findMethod(classR, "withAltLabels", String.class);
        if (withAltLabels == null) {
            throw new IllegalStateException("Method 'withAltLabels' not found for class : " + classR + " The class should implements XBuilder.With from @RecordBuilder");
        }
        var instanceWithNewAltLabel = ReflectionUtils.invokeMethod(withAltLabels, instance, newAltLabel);
        if (! classR.isInstance(instanceWithNewAltLabel)) {
            throw new IllegalStateException("Method 'withAltLabels' sor class : " + classR + " should return a type of "+classR+" instead of "+ getClassSafe(instanceWithNewAltLabel)+". Check that it implements XBuilder.With from @RecordBuilder");
        }
        return (R) instanceWithNewAltLabel;
    }

    private static Class<?> getClassSafe(Object object){
        return object == null ? null : object.getClass();
    }


}
