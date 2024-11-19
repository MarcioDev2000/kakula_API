package com.example.kakula.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 400 (BAD REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()); // Retorna apenas a mensagem de erro
    }

    // 401 (UNAUTHORIZED) - Exemplo de exceção personalizada para falta de autenticação
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleUnauthorizedException(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Usuário não autenticado. Por favor, faça login para continuar.");
        }
        throw ex; // Repassa a exceção caso não seja 401
    }

    // 403 (FORBIDDEN) - Exceção padrão para acesso negado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Acesso negado. Você não tem permissão para acessar este recurso.");
    }

    // 404 (NOT FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Recurso não encontrado.");
    }

    // 500 (INTERNAL SERVER ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.");
    }

    // Método para respostas de sucesso (200 e 201) - apenas exemplo
    public static ResponseEntity<String> successResponse(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(message);
    }

    // 400 (BAD REQUEST) - Tratamento de exceção para validações
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> {
        errors.put(error.getField(), error.getDefaultMessage());
    });
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
}

}