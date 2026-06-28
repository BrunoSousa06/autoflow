package com.autoflow.service.orcamento;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.ClienteOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.service.notificacao.MensagemNotificacao;
import com.autoflow.service.notificacao.NotificacaoService;
import com.autoflow.service.orcamento.impl.OrcamentoNotificacaoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrcamentoNotificacaoServiceImplTest {

    @Mock
    NotificacaoService notificacaoService;

    @InjectMocks
    OrcamentoNotificacaoServiceImpl service;

    @Test
    void naoDeveEnviarNotificacaoQuandoClienteNaoTemEmail() {
        OrcamentoEntity orcamento = orcamento(TipoOrcamento.PRINCIPAL);
        OrdemServicoEntity ordemServico = ordemServico(" ");

        service.enviarLinkOrcamentoParaCliente(orcamento, ordemServico, "https://autoflow.test/orcamentos/1");

        verify(notificacaoService, never()).enviar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveEnviarLinkDoOrcamentoPrincipalParaCliente() {
        OrcamentoEntity orcamento = orcamento(TipoOrcamento.PRINCIPAL);
        OrdemServicoEntity ordemServico = ordemServico("cliente@exemplo.com");

        service.enviarLinkOrcamentoParaCliente(orcamento, ordemServico, "https://autoflow.test/orcamentos/1");

        MensagemNotificacao mensagem = capturarMensagemEnviada();
        assertEquals("cliente@exemplo.com", mensagem.destinatario());
        assertTrue(mensagem.assunto().contains("AutoFlow"));
        assertTrue(mensagem.corpo().contains("#10"));
        assertTrue(mensagem.corpo().contains("OS-123"));
        assertTrue(mensagem.corpo().contains("https://autoflow.test/orcamentos/1"));
    }

    @Test
    void deveEnviarLinkDoReparoAdicionalParaCliente() {
        OrcamentoEntity orcamento = orcamento(TipoOrcamento.COMPLEMENTAR);
        OrdemServicoEntity ordemServico = ordemServico("cliente@exemplo.com");

        service.enviarLinkOrcamentoParaCliente(orcamento, ordemServico, "https://autoflow.test/orcamentos/10");

        MensagemNotificacao mensagem = capturarMensagemEnviada();
        assertEquals("cliente@exemplo.com", mensagem.destinatario());
        assertTrue(mensagem.assunto().contains("Orçamento complementar"));
        assertTrue(mensagem.corpo().contains("orçamento complementar"));
        assertTrue(mensagem.corpo().contains("#10"));
        assertTrue(mensagem.corpo().contains("https://autoflow.test/orcamentos/10"));
    }

    private MensagemNotificacao capturarMensagemEnviada() {
        ArgumentCaptor<MensagemNotificacao> captor = ArgumentCaptor.forClass(MensagemNotificacao.class);
        verify(notificacaoService).enviar(captor.capture());
        return captor.getValue();
    }

    private OrcamentoEntity orcamento(TipoOrcamento tipo) {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        orcamento.setId(10L);
        orcamento.setTipo(tipo);
        return orcamento;
    }

    private OrdemServicoEntity ordemServico(String emailCliente) {
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setNumeroOs("OS-123");
        ClienteOsEntity clienteOs = ClienteOsEntity.fromCliente(cliente("cliente@exemplo.com"));
        ReflectionTestUtils.setField(clienteOs, "email", emailCliente);
        ordemServico.setCliente(clienteOs);
        return ordemServico;
    }

    private ClienteEntity cliente(String email) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("Cliente");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail(email);
        cliente.setTelefone("11999999999");
        return cliente;
    }
}
