package com.autoflow.presentation.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoValidatorTest {

    @Test
    void deveValidarCpfFormatadoERejeitarVariacoesInvalidas() {
        assertTrue(DocumentoValidator.isCpf("529.982.247-25"));
        assertFalse(DocumentoValidator.isCpf(null));
        assertFalse(DocumentoValidator.isCpf("111.111.111-11"));
        assertFalse(DocumentoValidator.isCpf("529.982.247-26"));
        assertFalse(DocumentoValidator.isCpf("123"));
    }

    @Test
    void deveValidarCnpjFormatadoERejeitarVariacoesInvalidas() {
        assertTrue(DocumentoValidator.isCnpj("11.222.333/0001-81"));
        assertFalse(DocumentoValidator.isCnpj(null));
        assertFalse(DocumentoValidator.isCnpj("11.111.111/1111-11"));
        assertFalse(DocumentoValidator.isCnpj("11.222.333/0001-82"));
        assertFalse(DocumentoValidator.isCnpj("123"));
    }

    @Test
    void deveAceitarCpfOuCnpjERejeitarEntradaVazia() {
        assertTrue(DocumentoValidator.isCpfOuCnpj("529.982.247-25"));
        assertTrue(DocumentoValidator.isCpfOuCnpj("11.222.333/0001-81"));
        assertFalse(DocumentoValidator.isCpfOuCnpj(null));
        assertFalse(DocumentoValidator.isCpfOuCnpj("   "));
        assertFalse(DocumentoValidator.isCpfOuCnpj("documento invalido"));
    }
}
