package com.autoflow.domain.orcamento;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrcamentoEntity {
    private Long id;

    private Long ordemServicoId;

    private String numeroOs;

    private TipoOrcamento tipo;

    private Integer versao;

    private StatusOrcamento status;

    private LocalDateTime criadoEm;

    private LocalDateTime disponibilizadoEm;

    private BigDecimal totalServicos;

    private BigDecimal totalItens;

    private BigDecimal totalGeral;

    private String publicTokenHash;

    private LocalDateTime publicTokenExpiraEm;

    private LocalDateTime aprovadoEm;

    private LocalDateTime reprovadoEm;

    private String assinaturaNome;

    private String recusaMotivo;

    private List<OrcamentoServicoEntity> servicos;

    private List<OrcamentoItemNecessarioEntity> itens;

    @NonNull
    private ClienteOrcamentoSnapshot cliente;

    @NonNull
    private VeiculoOrcamentoSnapshot veiculo;

}

