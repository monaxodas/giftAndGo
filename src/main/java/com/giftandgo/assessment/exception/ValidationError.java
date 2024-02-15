package com.giftandgo.assessment.exception;

import java.util.Optional;

import lombok.Getter;

@Getter
public class ValidationError extends RuntimeException{
    private final String column;
    private final String errorMessage;

    public ValidationError(final String errorMessage, final String column) {
        this.column = column;
        this.errorMessage = errorMessage;
    }

    public ValidationError(final String errorMessage) {
        this.errorMessage = errorMessage;
        column = null;
    }

    public String getErrorMessage() {
        return Optional.ofNullable(column)
            .map(c -> "Validation error found at column: " + c + errorMessage)
            .orElse("Validation error occurred: " + errorMessage);
    }
}
