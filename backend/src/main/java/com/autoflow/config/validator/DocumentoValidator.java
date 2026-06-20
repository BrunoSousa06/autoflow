package com.autoflow.config.validator;

public final class DocumentoValidator {

    private DocumentoValidator() {
        throw new IllegalStateException("Classe utilitária");
    }

    public static boolean isCpfOuCnpj(String documento) {
        if (documento == null || documento.isBlank()) {
            return false;
        }

        String numero = documento.replaceAll("\\D", "");

        return isCpf(numero) || isCnpj(numero);
    }

    public static boolean isCpf(String cpf) {

        if (cpf == null) {
            return false;
        }

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int primeiroDigito = calcularDigitoCpf(cpf, 9);
        int segundoDigito = calcularDigitoCpf(cpf, 10);

        return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                && segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    public static boolean isCnpj(String cnpj) {

        if (cnpj == null) {
            return false;
        }

        cnpj = cnpj.replaceAll("\\D", "");

        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        int primeiroDigito = calcularDigitoCnpj(cnpj, 12);
        int segundoDigito = calcularDigitoCnpj(cnpj, 13);

        return primeiroDigito == Character.getNumericValue(cnpj.charAt(12))
                && segundoDigito == Character.getNumericValue(cnpj.charAt(13));
    }

    private static int calcularDigitoCpf(String cpf, int tamanhoBase) {

        int soma = 0;
        int peso = tamanhoBase + 1;

        for (int i = 0; i < tamanhoBase; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * peso--;
        }

        int resto = 11 - (soma % 11);

        return resto >= 10 ? 0 : resto;
    }

    private static int calcularDigitoCnpj(String cnpj, int tamanhoBase) {

        int soma = 0;
        int peso = 2;

        for (int i = tamanhoBase - 1; i >= 0; i--) {

            soma += Character.getNumericValue(cnpj.charAt(i)) * peso;

            peso++;

            if (peso > 9) {
                peso = 2;
            }
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}
