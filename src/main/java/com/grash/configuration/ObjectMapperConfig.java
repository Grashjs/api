package com.grash.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.grash.model.abstracts.Worker;
import com.grash.utils.WorkerDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {
    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Worker.class, new WorkerDeserializer());
        return new ObjectMapper().registerModules(module);
    }
}
