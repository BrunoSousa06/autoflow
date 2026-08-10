package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;
import com.autoflow.application.dto.notificacao.OrcamentoNotificacao;
import com.autoflow.application.gateway.NotificacaoGateway;
import com.autoflow.application.gateway.OrcamentoNotificacaoGateway;
import com.autoflow.domain.orcamento.TipoOrcamento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OrcamentoNotificacaoAdapter implements OrcamentoNotificacaoGateway {

    private final NotificacaoGateway notificacaoGateway;

    @Override
    public void notificar(OrcamentoNotificacao notificacao) {
        if (notificacao == null || !StringUtils.hasText(notificacao.clienteEmail())) {
            return;
        }

        String assunto = TipoOrcamento.COMPLEMENTAR.equals(notificacao.tipo())
                ? "Orçamento complementar aguardando aprovação - AutoFlow"
                : "Orçamento disponível - AutoFlow";
        String corpo = TipoOrcamento.COMPLEMENTAR.equals(notificacao.tipo())
                ? mensagemComplementar(notificacao)
                : mensagemPrincipal(notificacao);
        notificacaoGateway.enviar(new MensagemNotificacao(notificacao.clienteEmail(), assunto, corpo));
    }

    private String mensagemPrincipal(OrcamentoNotificacao notificacao) {
        return """
                Olá, %s.
                
                O orçamento #%d da sua ordem de serviço %s está disponível.
                
                Para baixar o PDF do orçamento, acesse o link abaixo:
                
                %s%s
                
                Atenciosamente,
                AutoFlow
                """.formatted(
                notificacao.clienteNome(), notificacao.orcamentoId(), notificacao.numeroOs(),
                notificacao.urlPublica(), trechoLinkDecisao(notificacao, "Para aprovar ou recusar o orçamento, acesse:"));
    }

    private String mensagemComplementar(OrcamentoNotificacao notificacao) {
        return """
                Olá, %s.
                
                Durante a execução da ordem de serviço %s, identificamos a necessidade de um orçamento complementar.
                
                O orçamento complementar #%d está disponível para sua análise e aprovação.
                
                Para baixar o PDF do orçamento complementar, acesse o link abaixo:
                
                %s%s
                
                Importante: este orçamento é complementar ao orçamento principal já aprovado.
                
                Atenciosamente,
                AutoFlow
                """.formatted(
                notificacao.clienteNome(), notificacao.numeroOs(), notificacao.orcamentoId(),
                notificacao.urlPublica(), trechoLinkDecisao(
                        notificacao,
                        "Para aprovar ou recusar o orçamento complementar, acesse:"));
    }

    private String trechoLinkDecisao(OrcamentoNotificacao notificacao, String label) {
        return StringUtils.hasText(notificacao.urlDecisao())
                ? "\n\n" + label + "\n\n" + notificacao.urlDecisao() + "\n"
                : "";
    }
}
