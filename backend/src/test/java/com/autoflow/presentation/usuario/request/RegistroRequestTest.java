package com.autoflow.presentation.usuario.request;

import com.autoflow.domain.usuario.RoleEnum;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void devePermitirStaffSemCpfCnpjETelefone() {
        var request = new RegistroRequest(
                "Atendente",
                "atendente@autoflow.com",
                null,
                null,
                "Senha@1234",
                RoleEnum.ATENDENTE);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void deveExigirCpfCnpjETelefoneParaCliente() {
        var request = new RegistroRequest(
                "Cliente",
                "cliente@autoflow.com",
                null,
                null,
                "Senha@1234",
                RoleEnum.CLIENTE);

        var camposInvalidos = validator.validate(request).stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .sorted()
                .toList();

        assertEquals(java.util.List.of("cpfCnpj", "telefone"), camposInvalidos);
    }
}
