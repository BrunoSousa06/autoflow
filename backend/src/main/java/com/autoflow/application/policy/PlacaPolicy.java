package com.autoflow.application.policy;

public final class PlacaPolicy {

    private PlacaPolicy() {
    }

    public static String normalizar(String placa) {
        if (placa == null) {
            return null;
        }

        return placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
