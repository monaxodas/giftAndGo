package com.giftandgo.assessment.exception;

import org.springframework.http.HttpStatus;

public class GeolocationException extends HttpException {
    public GeolocationException(final String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
