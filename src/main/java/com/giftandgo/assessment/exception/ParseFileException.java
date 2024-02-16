package com.giftandgo.assessment.exception;

import org.springframework.http.HttpStatus;

public class ParseFileException extends HttpException {

    public ParseFileException(final Throwable throwable) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
    }
}
