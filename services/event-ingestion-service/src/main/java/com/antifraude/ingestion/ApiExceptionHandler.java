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

/**
 * Handler de exceções globais para a API de Ingestão de Eventos.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Trata erros de validação de argumentos de método (Bean Validation).
     *
     * @param ex Exceção capturada.
     * @return DTO ApiErrorResponse.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationError(final MethodArgumentNotValidException ex) {
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

    /**
     * Trata erros de leitura de payload HTTP (JSON malformado).
     *
     * @param ex Exceção capturada.
     * @return DTO ApiErrorResponse.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleUnreadablePayload(final HttpMessageNotReadableException ex) {
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
