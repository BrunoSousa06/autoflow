package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarOrcamentoPorTokenUseCaseTest {
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock OrcamentoPublicacaoGateway publicacaoGateway;
    @InjectMocks ConsultarOrcamentoPorTokenUseCase useCase;

    @Test
    void deveNegarTokenInvalido() {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        when(orcamentoGateway.findById(1L)).thenReturn(Optional.of(orcamento));
        when(publicacaoGateway.validarToken(orcamento, "invalido")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> useCase.execute(1L, "invalido"));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}
