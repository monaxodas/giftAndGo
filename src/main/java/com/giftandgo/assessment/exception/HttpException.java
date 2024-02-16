package com.giftandgo.assessment.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class HttpException extends RuntimeException {
    private final HttpStatus status;

    public HttpException(final HttpStatus status, final String message) {
        super(message);
        this.status = status;
    }
}
