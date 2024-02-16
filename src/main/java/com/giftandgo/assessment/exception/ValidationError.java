package com.giftandgo.assessment.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ValidationError extends HttpException{
    public ValidationError(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
