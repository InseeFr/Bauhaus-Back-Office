package fr.insee.rmes.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.EnumSet;

@Configuration
public class WebServiceConfiguration {


    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> eTagFilter(){
        return registration(new ShallowEtagHeaderFilter());
    }

    @NotNull
    private <T extends Filter> FilterRegistrationBean<T> registration(T registred) {
        var registration = new
                FilterRegistrationBean<>(registred);
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CacheControlFilter> cacheControlFilter(){
        return registration(new CacheControlFilter());
    }


}
