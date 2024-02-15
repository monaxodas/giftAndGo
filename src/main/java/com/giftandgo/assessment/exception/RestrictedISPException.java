package com.giftandgo.assessment.exception;


public class RestrictedISPException extends RuntimeException {
    private final String isp;

    public RestrictedISPException(final String isp) {
        super("Requests from Isp/Data center: " + isp + " are not allowed to invoke this API");
        this.isp = isp;
    }

}
