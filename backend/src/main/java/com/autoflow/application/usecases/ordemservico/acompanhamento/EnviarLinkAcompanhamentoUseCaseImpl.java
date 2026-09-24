package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.gateway.NotificacaoGateway;
import com.autoflow.application.input.notificacao.MensagemNotificacao;
import com.autoflow.application.port.in.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCase;
import com.autoflow.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnviarLinkAcompanhamentoUseCaseImpl implements EnviarLinkAcompanhamentoUseCase {

    private final NotificacaoGateway notificacaoGateway;
    private final String frontendPublicBaseUrl;

    @Override
    public void execute(OrdemServico ordemServico, String token) {
        String emailCliente = ordemServico.getCliente().getEmail();
        if (emailCliente == null || emailCliente.isBlank()) {
            return;
        }

        String url = frontendPublicBaseUrl + "/public/acompanhamento?token=" + token;
        notificacaoGateway.enviar(new MensagemNotificacao(
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
