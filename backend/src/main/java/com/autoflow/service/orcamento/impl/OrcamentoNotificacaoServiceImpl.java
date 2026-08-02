package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.application.dto.notificacao.MensagemNotificacao;
import com.autoflow.application.gateway.NotificacaoService;
import com.autoflow.service.orcamento.OrcamentoNotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OrcamentoNotificacaoServiceImpl implements OrcamentoNotificacaoService {

    private final NotificacaoService notificacaoService;

    @Override
    public void enviarLinkOrcamentoParaCliente(
            OrcamentoEntity orcamento,
            OrdemServicoEntity ordemServico,
            String urlPublica
    ) {
        String emailCliente = ordemServico.getCliente().getEmail();

        if (!StringUtils.hasText(emailCliente)) {
            return;
        }

        MensagemNotificacao mensagem = new MensagemNotificacao(
                emailCliente,
                montarAssunto(orcamento),
                montarMensagem(ordemServico, orcamento, urlPublica)
        );

        notificacaoService.enviar(mensagem);
    }

    private String montarAssunto(OrcamentoEntity orcamento) {
        if (TipoOrcamento.COMPLEMENTAR.equals(orcamento.getTipo())) {
            return "Orçamento complementar aguardando aprovação - AutoFlow";
        }

        return "Orçamento disponível - AutoFlow";
    }

    private String montarMensagem(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamento,
            String urlPublica
    ) {
        if (TipoOrcamento.COMPLEMENTAR.equals(orcamento.getTipo())) {
            return montarMensagemReparoAdicional(ordemServico, orcamento, urlPublica);
        }

        return montarMensagemOrcamentoPrincipal(ordemServico, orcamento, urlPublica);
    }

    private String montarMensagemOrcamentoPrincipal(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamento,
            String urlPublica
    ) {
        return """
            Olá, %s.

            O orçamento #%d da sua ordem de serviço %s está disponível.

            Para baixar o PDF do orçamento, acesse o link abaixo:

            %s

            Atenciosamente,
            AutoFlow
            """.formatted(
                ordemServico.getCliente().getNome(),
                orcamento.getId(),
                ordemServico.getNumeroOs(),
                urlPublica
        );
    }

    private String montarMensagemReparoAdicional(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamento,
            String urlPublica
    ) {
        return """
            Olá, %s.

            Durante a execução da ordem de serviço %s, identificamos a necessidade de um orçamento complementar.

            O orçamento complementar #%d está disponível para sua análise e aprovação.

            Para baixar o PDF do orçamento complementar, acesse o link abaixo:

            %s

            Importante: este orçamento é complementar ao orçamento principal já aprovado.

            Atenciosamente,
            AutoFlow
            """.formatted(
                ordemServico.getCliente().getNome(),
                ordemServico.getNumeroOs(),
                orcamento.getId(),
                urlPublica
        );
    }
}
