package com.autoflow.domain;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.ordemservico.StatusServicoOs;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class ServicoSolicitadoEntityTest {

    @Test
    void deveCriarServicoSolicitado() {
        Long servicoId = 1L;

        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(servicoId, "Alinhamento");

        assertEquals(servicoId, servico.getServicoId());
        assertEquals("Alinhamento", servico.getNome());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitadoEntity(null, "Alinhamento")),
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitadoEntity(1L, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitadoEntity(1L, " ")),
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitadoEntity(1L, "Alinhamento", null))
        );
    }

    @Test
    void deveCriarServicoComValorEStatusAguardando() {
        ServicoSolicitadoEntity servico = ServicoSolicitadoEntity.criar(
                1L,
                "Alinhamento",
                new BigDecimal("120.00")
        );

        assertEquals(1L, servico.getServicoId());
        assertEquals("Alinhamento", servico.getNome());
        assertEquals(new BigDecimal("120.00"), servico.getValor());
        assertEquals(StatusServicoOs.AGUARDANDO, servico.getStatus());
    }

    @Test
    void deveRegistrarItensNecessariosSubstituindoListaAtual() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Alinhamento", new BigDecimal("120.00"));
        ItemNecessarioEntity itemInicial = item(1L, "Peca 1", 1);
        ItemNecessarioEntity itemNovo = item(2L, "Peca 2", 2);

        servico.registrarItensNecessarios(List.of(itemInicial));
        servico.registrarItensNecessarios(List.of(itemNovo));

        assertEquals(1, servico.getItensNecessarios().size());
        assertEquals(2L, servico.getItensNecessarios().getFirst().getPecaInsumoId());
        assertEquals("Peca 2", servico.getItensNecessarios().getFirst().getNome());
    }

    @Test
    void deveIniciarServicoAtualizandoStatusItensEData() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Alinhamento", new BigDecimal("120.00"));
        ItemNecessarioEntity itemAtualizado = item(1L, "Peca 1", 1);

        servico.iniciar(List.of(itemAtualizado));

        assertEquals(StatusServicoOs.EM_EXECUCAO, servico.getStatus());
        assertNotNull(servico.getIniciadoEm());
        assertEquals(List.of(itemAtualizado), servico.getItensNecessarios());
    }

    @Test
    void naoDeveIniciarServicoFinalizado() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Alinhamento", new BigDecimal("120.00"));
        servico.iniciar(List.of(item(1L, "Peca 1", 1)));
        servico.finalizar();
        List<ItemNecessarioEntity> itensAtualizados = List.of(item(2L, "Peca 2", 1));

        assertThrows(IllegalStateException.class, () -> servico.iniciar(itensAtualizados));
    }

    @Test
    void deveFinalizarServicoEmExecucao() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Alinhamento", new BigDecimal("120.00"));
        servico.iniciar(List.of(item(1L, "Peca 1", 1)));

        servico.finalizar();

        assertEquals(StatusServicoOs.FINALIZADO, servico.getStatus());
        assertNotNull(servico.getFinalizadoEm());
    }

    @Test
    void naoDeveFinalizarServicoQueNaoEstaEmExecucao() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Alinhamento", new BigDecimal("120.00"));

        assertThrows(IllegalStateException.class, servico::finalizar);
    }

    private ItemNecessarioEntity item(Long id, String nome, int quantidade) {
        return ItemNecessarioEntity.criar(
                id,
                nome,
                CategoriaPecaInsumo.PECA,
                new BigDecimal("10.00"),
                quantidade,
                StatusItemNecessario.DISPONIVEL
        );
    }
}
