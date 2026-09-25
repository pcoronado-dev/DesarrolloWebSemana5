package com.tecsup.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarErrores(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errores.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errores);
    }

    // Falta de credenciales o usuario/rol invalidos
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> manejarUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    // Usuario valido pero sin permisos para la operacion
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> manejarForbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    // Manejo general de errores inesperados (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErroresGenerales(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", "Error interno del servidor: " + ex.getMessage());
        return ResponseEntity.status(500).body(error);
    }
}