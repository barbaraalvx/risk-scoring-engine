package com.antifraude.ingestion;

public record ValidationErrorDetail(String field, String message, Object rejectedValue) {
}
