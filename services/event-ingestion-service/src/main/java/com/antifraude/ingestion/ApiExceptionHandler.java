package com.antifraude.ingestion;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationError(final MethodArgumentNotValidException ex) {
        // Traduz erros de Bean Validation para um payload uniforme, amigavel para API clients.
        List<ValidationErrorDetail> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationErrorDetail)
                .collect(Collectors.toList());

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Payload invalido.",
                details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleUnreadablePayload(final HttpMessageNotReadableException ex) {
        // Falha de parse JSON acontece antes da validacao de campos, por isso trata separadamente.
        ValidationErrorDetail detail = new ValidationErrorDetail("body", "JSON invalido ou mal formatado.", null);

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Payload invalido.",
                List.of(detail));
    }

    private ValidationErrorDetail toValidationErrorDetail(final FieldError error) {
        return new ValidationErrorDetail(error.getField(), error.getDefaultMessage(), error.getRejectedValue());
    }
}
