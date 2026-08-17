package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.dto.ordemservico.StatusOrdemServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.dto.security.CurrentUser;
import com.autoflow.domain.ordemservico.ClienteOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarStatusOrdemServicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;
    @Mock
    private CurrentUserGateway currentUserGateway;
    @Mock
    private VeiculoClienteGateway clienteGateway;

    @ParameterizedTest
    @EnumSource(StatusOrdemServico.class)
    void deveRetornarOsStatusEUltimaAtualizacaoPersistidos(StatusOrdemServico status) {
        LocalDateTime ultimaAtualizacao = LocalDateTime.of(2026, 5, 30, 12, 0);
        OrdemServico ordemServico = criarOrdemServico(status, ultimaAtualizacao, 10L);
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico));
        when(currentUserGateway.getCurrentUser())
                .thenReturn(Optional.of(new CurrentUser("atendente@autoflow.com", RoleEnum.ATENDENTE)));

        StatusOrdemServicoOutput output = novoUseCase().execute("OS-123", "atendente@autoflow.com");

        assertEquals("OS-123", output.numeroOs());
        assertEquals(status, output.status());
        assertEquals(ultimaAtualizacao, output.ultimaAtualizacao());
        verifyNoInteractions(clienteGateway);
    }

    @Test
    void deveRetornarStatusParaClienteTitular() {
        OrdemServico ordemServico = criarOrdemServico(
                StatusOrdemServico.EM_EXECUCAO,
                LocalDateTime.of(2026, 5, 30, 12, 0),
                10L);
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico));
        when(currentUserGateway.getCurrentUser())
                .thenReturn(Optional.of(new CurrentUser("cliente@autoflow.com", RoleEnum.CLIENTE)));
        when(clienteGateway.findIdByUsuarioEmail("cliente@autoflow.com"))
                .thenReturn(Optional.of(10L));

        StatusOrdemServicoOutput output = novoUseCase().execute("OS-123", "cliente@autoflow.com");

        assertEquals(StatusOrdemServico.EM_EXECUCAO, output.status());
        verify(clienteGateway).findIdByUsuarioEmail("cliente@autoflow.com");
    }

    @Test
    void deveRecusarClienteNaoTitular() {
        OrdemServico ordemServico = criarOrdemServico(
                StatusOrdemServico.RECEBIDA,
                LocalDateTime.of(2026, 5, 30, 12, 0),
                10L);
        when(ordemServicoGateway.findByNumeroOs("OS-123")).thenReturn(Optional.of(ordemServico));
        when(currentUserGateway.getCurrentUser())
                .thenReturn(Optional.of(new CurrentUser("outro@autoflow.com", RoleEnum.CLIENTE)));
        when(clienteGateway.findIdByUsuarioEmail("outro@autoflow.com"))
                .thenReturn(Optional.of(20L));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> novoUseCase().execute("OS-123", "outro@autoflow.com"));

        assertEquals(ApplicationException.ErrorType.FORBIDDEN, exception.type());
        assertEquals("Você não tem permissão para acessar esta ordem de serviço.", exception.getMessage());
    }

    @Test
    void deveLancar404QuandoOsNaoExistir() {
        when(ordemServicoGateway.findByNumeroOs("OS-404")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> novoUseCase().execute("OS-404", "atendente@autoflow.com"));

        assertEquals(ApplicationException.ErrorType.NOT_FOUND, exception.type());
        assertEquals("Ordem de serviço não encontrada.", exception.getMessage());
        verifyNoInteractions(currentUserGateway, clienteGateway);
    }

    private ConsultarStatusOrdemServicoUseCase novoUseCase() {
        return new ConsultarStatusOrdemServicoUseCase(
                ordemServicoGateway,
                currentUserGateway,
                clienteGateway);
    }

    private OrdemServico criarOrdemServico(
            StatusOrdemServico status,
            LocalDateTime ultimaAtualizacao,
            Long clienteId) {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setNumeroOs("OS-123");
        ordemServico.setStatus(status);
        ordemServico.setUltimaAtualizacao(ultimaAtualizacao);
        ordemServico.setCliente(ClienteOs.fromFields(
                clienteId,
                "Cliente Teste",
                "52998224725",
                "cliente@autoflow.com",
                "11999999999"));
        return ordemServico;
    }
}
