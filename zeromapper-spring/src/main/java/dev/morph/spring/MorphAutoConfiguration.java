package dev.morph.spring;

import dev.morph.Mapper;
import dev.morph.engine.DefaultMappingEngine;
import dev.morph.engine.MappingEngine;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for Morph.
 */
@AutoConfiguration
@EnableConfigurationProperties(MorphProperties.class)
public class MorphAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MappingEngine mappingEngine() {
        return DefaultMappingEngine.getInstance();
    }

    @Bean
    @ConditionalOnMissingBean
    MorphMapper morphMapper(MappingEngine mappingEngine) {
        Mapper.useEngine(mappingEngine);
        return new MorphMapper();
    }
}
