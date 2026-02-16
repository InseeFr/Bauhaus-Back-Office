package fr.insee.rmes.modules.commons.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.function.Supplier;

public class PropertiesLogger implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger log= LoggerFactory.getLogger(PropertiesLogger.class);
    public static final String PROPERTY_KEY_FOR_PREFIXES = "fr.insee.properties.log.key.prefixes";
    public static final String PROPERTY_KEY_FOR_MORE_HIDDEN = "fr.insee.properties.log.key.hidden.more";
    public static final String PROPERTY_KEY_FOR_SOURCES_IGNORED = "fr.insee.properties.log.sources.ignored";
    public static final String PROPERTY_KEY_FOR_SOURCES_SELECT = "fr.insee.properties.log.key.select";
    private static final Set<String> baseMotsCaches = Set.of("password", "pwd", "jeton", "token", "secret", "credential", "pw");
    private static final Set<String> prefixesAffichesParDefaut= Set.of("fr.insee","logging","keycloak","spring","application","server","springdoc","management","minio");
    private static final Set<String> propertySourcesIgnoreesParDefaut = Set.of("systemProperties", "systemEnvironment");
    private static final PropertySelectorEnum PROPERTY_SELECTOR_PAR_DEFAUT = PropertySelectorEnum.PREFIX;


    private final Collection<String> propertySourceNames=new ArrayList<>();
    private Set<String> hiddensProps;
    private Set<String> ignoredPropertySources;
    private PropertySelector propertySelector;
    private static Set<String> prefixForSelectedProps;

    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        Environment environment=event.getEnvironment();

        var props= new StringBuilder();
        this.hiddensProps = getMoreHiddenPropsFromPropertyAndMerge(environment);
        prefixForSelectedProps = environment.getProperty(PROPERTY_KEY_FOR_PREFIXES, Set.class, prefixesAffichesParDefaut);
        this.ignoredPropertySources = environment.getProperty(PROPERTY_KEY_FOR_SOURCES_IGNORED, Set.class, propertySourcesIgnoreesParDefaut);
        var propertySelectorType=this.getSelectorFromProperty(environment.getProperty(PROPERTY_KEY_FOR_SOURCES_SELECT))
                .orElse(PROPERTY_SELECTOR_PAR_DEFAUT);
        debug(()->"Logging "+propertySelectorType.forLogging());
        this.propertySelector=propertySelectorType.propertySelector();

        ((AbstractEnvironment) environment).getPropertySources().stream()
                .filter(this::isEnumerable)
                .filter(this::sourceWillBeProcessed)
                .map(this::rememberPropertySourceNameThenCast)
                .map(EnumerablePropertySource::getPropertyNames)
                .flatMap(Arrays::stream)
                .distinct()
                .filter(Objects::nonNull)
                .filter(this::filterFromPropertySelector)
                .forEach(key-> props.append(key).append(" = ")
                        .append(resoutValeurAvecMasquePwd(key, environment))
                        .append(System.lineSeparator()));
        props.append("============================================================================");
        props.insert(0, """
                ===============================================================================================
                                                Valeurs des properties pour :
                %s
                ===============================================================================================
                """.formatted(this.propertySourceNames.stream().reduce("",(l, e)->l+System.lineSeparator()+"- "+e )));
        info(props::toString);

    }

    private static Set<String> getMoreHiddenPropsFromPropertyAndMerge(Environment environment) {
        Set<String> moreProps = environment.getProperty(PROPERTY_KEY_FOR_MORE_HIDDEN, Set.class);
        var retour = baseMotsCaches;
        if (moreProps != null){
            retour= new HashSet<>(moreProps);
            retour.addAll(baseMotsCaches);
        }
        return retour;
    }

    private Optional<PropertySelectorEnum> getSelectorFromProperty(String property) {
        if(property!=null){
            try{
                return Optional.of(PropertySelectorEnum.valueOf(property));
            }catch (IllegalArgumentException _){
                trace(()->"Impossible de convertir "+property+" en une constante de PropertySelectorEnum. Le PropertySelector par défaut sera utilisé.");
            }
        }
        return Optional.empty();
    }

    private boolean filterFromPropertySelector(@NonNull String s) {
        if (! this.propertySelector.filter(s)){
            debug(()->s+ " ne commence pas par un des prefix retenus pour être loguée");
            return false;
        }
        return true;
    }

    private boolean sourceWillBeProcessed(PropertySource<?> propertySource) {

        if (ignoredPropertySources.contains(propertySource.getName())){
            debug(()->propertySource+ " sera ignorée");
            return false;
        }
        return true;
    }

    private EnumerablePropertySource<?> rememberPropertySourceNameThenCast(PropertySource<?> propertySource) {
        this.propertySourceNames.add(propertySource.getName());
        return (EnumerablePropertySource<?>) propertySource;
    }

    private boolean isEnumerable(PropertySource<?> propertySource) {
        if (! (propertySource instanceof EnumerablePropertySource)){
            debug(()->propertySource+ " n'est pas EnumerablePropertySource : impossible à lister");
            return false;
        }
        return true;
    }

    private void debug(Supplier<String> messageForDebug) {
        if (log.isDebugEnabled()){
            log.debug(messageForDebug.get());
        }
    }

    private void trace(Supplier<String> messageForDebug) {
        if (log.isTraceEnabled()){
            log.trace(messageForDebug.get());
        }
    }

    private void info(Supplier<String> messageForDebug) {
        if (log.isInfoEnabled()){
            log.info(messageForDebug.get());
        }
    }

    private Object resoutValeurAvecMasquePwd(String key, Environment environment) {
        if (hiddensProps.stream().anyMatch(key::contains)) {
            return "******";
        }
        return environment.getProperty(key);

    }


    @FunctionalInterface
    private interface PropertySelector {
        boolean filter(String s);
    }

    private enum PropertySelectorEnum {
        ALL(_->true, ()->"all properties"),
        NONE(_->false, ()->"no properties"),
        PREFIX(k->prefixForSelectedProps.stream().anyMatch(k::startsWith), () -> "properties starting with "+ prefixForSelectedProps);

        private final PropertySelector propertySelector;
        private final Supplier<String> logString;

        PropertySelectorEnum(PropertySelector propertySelector, Supplier<String> logString) {
            this.propertySelector=propertySelector;
            this.logString=logString;
        }

        public PropertySelector propertySelector() {
            return propertySelector;
        }

        public String forLogging(){
            return logString.get();
        }

    }
}

