package com.autoflow.presentation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DadosClienteValidosValidator.class)
public @interface DadosClienteValidos {

    String message() default "Dados do cliente inválidos";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
