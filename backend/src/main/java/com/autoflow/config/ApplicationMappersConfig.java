package com.autoflow.config;

import com.autoflow.application.mapper.UsuarioApplicationMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationMappersConfig {
    @Bean
    public UsuarioApplicationMapper usuarioApplicationMapper() {
        return Mappers.getMapper(UsuarioApplicationMapper.class);
    }
}
