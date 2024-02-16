package com.giftandgo.assessment.exception;

import org.springframework.http.HttpStatus;

public class RestrictedCountryException extends HttpException {

    public RestrictedCountryException(final String country) {
        super(HttpStatus.FORBIDDEN, "Country " + country + " is not allowed to invoke this API");
    }
}
