package com.tecsup.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String mensaje) {
        super(mensaje);
    }
}