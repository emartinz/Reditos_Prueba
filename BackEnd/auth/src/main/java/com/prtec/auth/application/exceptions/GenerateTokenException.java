package com.prtec.auth.application.exceptions;

public class GenerateTokenException extends RuntimeException {

    // Constructor por defecto
    public GenerateTokenException() {
        super();
    }

    // Constructor con un mensaje personalizado
    public GenerateTokenException(String message) {
        super(message);
    }

    // Constructor con un mensaje y una causa (otra excepción)
    public GenerateTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}