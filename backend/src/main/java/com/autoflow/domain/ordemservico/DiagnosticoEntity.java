package com.autoflow.domain.ordemservico;

import com.autoflow.domain.usuario.Usuario;
import com.autoflow.infrastructure.persistence.entity.usuario.UsuarioEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Embeddable
public class DiagnosticoEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_mecanico_id")
    private UsuarioEntity mecanicoEntity;

    @Transient
    private Usuario mecanico;

    private static Usuario toDomain(UsuarioEntity entity) {
        Usuario usuario = new Usuario();
        usuario.setId(entity.getId());
        usuario.setNome(entity.getNome());
        usuario.setEmail(entity.getEmail());
        usuario.setSenha(entity.getSenha());
        usuario.setRole(entity.getRole());
        return usuario;
    }

    private static UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(usuario.getId());
        entity.setNome(usuario.getNome());
        entity.setEmail(usuario.getEmail());
        entity.setSenha(usuario.getSenha());
        entity.setRole(usuario.getRole());
        return entity;
    }

    @Transient
    public Usuario getMecanico() {
        if (mecanico == null && mecanicoEntity != null) {
            mecanico = toDomain(mecanicoEntity);
        }
        return mecanico;
    }

    public void setMecanico(Usuario mecanico) {
        this.mecanico = mecanico;
        this.mecanicoEntity = toEntity(mecanico);
    }

    @Column(name = "diagnostico_iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "diagnostico_concluido_em")
    private LocalDateTime concluidoEm;

    @Column(name = "diagnostico_laudo")
    private String laudo;
}
