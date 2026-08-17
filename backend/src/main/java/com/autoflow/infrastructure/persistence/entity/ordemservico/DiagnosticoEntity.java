package com.autoflow.infrastructure.persistence.entity.ordemservico;

import com.autoflow.infrastructure.persistence.entity.usuario.UsuarioEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class DiagnosticoEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_mecanico_id")
    private UsuarioEntity mecanico;

    @Column(name = "diagnostico_iniciado_em")
    private LocalDateTime iniciadoEm;

    @Column(name = "diagnostico_concluido_em")
    private LocalDateTime concluidoEm;

    @Column(name = "diagnostico_laudo")
    private String laudo;
}
