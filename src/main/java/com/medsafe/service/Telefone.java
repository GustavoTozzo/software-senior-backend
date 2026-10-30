package com.medsafe.service;

import com.medsafe.exception.BusinessException;

/** O telefone e o login do usuario, entao e guardado so com digitos para comparar sem depender da mascara. */
final class Telefone {

    private static final int MINIMO_DIGITOS = 10;
    private static final int MAXIMO_DIGITOS = 13;

    private Telefone() {
    }

    static String normalizar(String telefone) {
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (digitos.length() < MINIMO_DIGITOS || digitos.length() > MAXIMO_DIGITOS) {
            throw new BusinessException("Informe o telefone com DDD, por exemplo (11) 99999-0000");
        }
        return digitos;
    }
}
