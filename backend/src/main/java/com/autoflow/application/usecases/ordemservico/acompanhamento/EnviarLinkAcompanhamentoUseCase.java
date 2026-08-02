package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;
import com.autoflow.application.gateway.NotificacaoService;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EnviarLinkAcompanhamentoUseCase {

    private final NotificacaoService notificacaoService;

    @Value("${app.frontend-public-base-url}")
    private String frontendPublicBaseUrl;

    public void execute(OrdemServicoEntity ordemServico, String token) {
        String emailCliente = ordemServico.getCliente().getEmail();
        if (!StringUtils.hasText(emailCliente)) {
            return;
        }

        String url = frontendPublicBaseUrl + "/public/acompanhamento?token=" + token;
        notificacaoService.enviar(new MensagemNotificacao(
                emailCliente,
                "Acompanhe sua ordem de serviço " + ordemServico.getNumeroOs() + " - AutoFlow",
                """
                Olá, %s.

                Sua ordem de serviço %s foi criada com sucesso.

                Você pode acompanhar o andamento pelo link abaixo:

                %s

                Se preferir, também pode acessar o AutoFlow com seu login e senha e consultar suas ordens em Minha Conta.

                Este link é pessoal. Não o compartilhe com terceiros.

                Atenciosamente,
                AutoFlow
                """.formatted(ordemServico.getCliente().getNome(), ordemServico.getNumeroOs(), url)
        ));
    }
}
