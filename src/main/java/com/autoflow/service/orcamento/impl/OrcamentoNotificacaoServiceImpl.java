package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.service.notificacao.MensagemNotificacao;
import com.autoflow.service.notificacao.NotificacaoService;
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
                "Orçamento disponível - AutoFlow",
                montarMensagem(ordemServico, orcamento, urlPublica)
        );

        notificacaoService.enviar(mensagem);
    }

    private String montarMensagem(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamento,
            String urlPublica
    ) {
        return """
                Olá, %s.

                O orçamento #%d da sua ordem de serviço está disponível.

                Para baixar o PDF do orçamento, acesse o link abaixo:

                %s

                Atenciosamente,
                AutoFlow
                """.formatted(
                ordemServico.getCliente().getNome(),
                orcamento.getId(),
                urlPublica
        );
    }
}