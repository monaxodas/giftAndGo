package com.giftandgo.assessment.exception;

public class RestrictedCountryException extends RuntimeException {
    private final String country;

    public RestrictedCountryException(final String country) {
        super("Country" + country + " is not allowed to invoke this API");
        this.country = country;
    }
}
