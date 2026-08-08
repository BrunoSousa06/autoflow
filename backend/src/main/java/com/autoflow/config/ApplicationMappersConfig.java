package com.autoflow.config;

import com.autoflow.application.mapper.PecaInsumoMapper;
import com.autoflow.application.mapper.UsuarioApplicationMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationMappersConfig {
    @Bean
    public PecaInsumoMapper pecaInsumoMapper() {
        return Mappers.getMapper(PecaInsumoMapper.class);
    }

    @Bean
    public UsuarioApplicationMapper usuarioApplicationMapper() {
        return Mappers.getMapper(UsuarioApplicationMapper.class);
    }
}
