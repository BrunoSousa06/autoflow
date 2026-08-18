package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentoPorTokenUseCase;
import com.autoflow.domain.orcamento.Orcamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarOrcamentoPorTokenUseCaseTest {
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock OrcamentoPublicacaoGateway publicacaoGateway;
    @InjectMocks ConsultarOrcamentoPorTokenUseCaseImpl useCase;

    @Test
    void deveNegarTokenInvalido() {
        Orcamento orcamento = new Orcamento();
        when(orcamentoGateway.findById(1L)).thenReturn(Optional.of(orcamento));
        when(publicacaoGateway.validarToken(orcamento, "invalido")).thenReturn(false);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> useCase.execute(1L, "invalido"));
        assertEquals(ApplicationException.ErrorType.UNAUTHORIZED, exception.type());
    }
}
