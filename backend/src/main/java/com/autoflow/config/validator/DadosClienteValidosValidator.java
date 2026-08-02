package com.autoflow.config.validator;

import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.presentation.usuario.request.RegistroRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class DadosClienteValidosValidator
        implements ConstraintValidator<DadosClienteValidos, RegistroRequest> {

    @Override
    public boolean isValid(RegistroRequest request, ConstraintValidatorContext context) {
        if (request == null || !RoleEnum.CLIENTE.equals(request.role())) {
            return true;
        }

        boolean valido = true;
        context.disableDefaultConstraintViolation();

        if (!DocumentoValidator.isCpfOuCnpj(request.cpfCnpj())) {
            adicionarErro(context, "cpfCnpj", "CPF/CNPJ inválido");
            valido = false;
        }

        if (StringUtils.isBlank(request.telefone())) {
            adicionarErro(context, "telefone", "O telefone é obrigatorio");
            valido = false;
        }

        return valido;
    }

    private void adicionarErro(
            ConstraintValidatorContext context,
            String campo,
            String mensagem) {
        context.buildConstraintViolationWithTemplate(mensagem)
                .addPropertyNode(campo)
                .addConstraintViolation();
    }
}
