package com.autoflow.domain.ordemServico;

import com.autoflow.domain.usuario.UsuarioEntity;
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
    private UsuarioEntity mecanico;

    @Column(name = "diagnostico_iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "diagnostico_concluido_em")
    private LocalDateTime concluidoEm;

    @Column(name = "laudo")
    private String laudo;
}