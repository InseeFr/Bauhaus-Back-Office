package fr.insee.rmes.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Configuration
public class WebServiceConfiguration {


    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> eTagFilter(){
        var registration = new
                FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }


}
