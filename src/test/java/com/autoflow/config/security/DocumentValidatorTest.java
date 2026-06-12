package com.autoflow.config.security;

import com.autoflow.config.validator.DocumentoValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentValidatorTest {

    @Test
    void deveRetornarFalseQuandoDocumentoForNull() {
        assertFalse(DocumentoValidator.isCpfOuCnpj(null));
    }

    @Test
    void deveRetornarFalseQuandoDocumentoForVazio() {
        assertFalse(DocumentoValidator.isCpfOuCnpj(""));
    }

    @Test
    void deveRetornarFalseQuandoDocumentoForBlank() {
        assertFalse(DocumentoValidator.isCpfOuCnpj("   "));
    }

    @Test
    void deveRetornarTrueParaCpfValido() {
        assertTrue(DocumentoValidator.isCpfOuCnpj("52998224725"));
    }

    @Test
    void deveRetornarTrueParaCnpjValido() {
        assertTrue(DocumentoValidator.isCpfOuCnpj("11444777000161"));
    }

    @Test
    void deveRetornarFalseParaDocumentoInvalido() {
        assertFalse(DocumentoValidator.isCpfOuCnpj("123"));
    }
}
