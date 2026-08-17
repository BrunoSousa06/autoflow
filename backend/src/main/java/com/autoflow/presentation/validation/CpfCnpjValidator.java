package com.autoflow.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class CpfCnpjValidator implements ConstraintValidator<CpfCnpj, String> {

    @Override
    public boolean isValid(String value,
                           ConstraintValidatorContext context) {

        return DocumentoValidator.isCpfOuCnpj(value);
    }

}
