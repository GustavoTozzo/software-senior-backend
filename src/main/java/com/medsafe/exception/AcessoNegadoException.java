package com.medsafe.exception;

public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException() {
        super("Você não tem permissão para acessar este recurso");
    }
}
