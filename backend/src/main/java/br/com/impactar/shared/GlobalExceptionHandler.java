package br.com.impactar.shared;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Respostas de erro no formato ProblemDetail; o front exibe o campo "detail". */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> handleApiException(ApiException ex) {
        return problem(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Dados inválidos.");
        return problem(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Requisição inválida.");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
    }
}
