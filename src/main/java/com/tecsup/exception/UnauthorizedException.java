package com.tecsup.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String mensaje) {
        super(mensaje);
    }
}