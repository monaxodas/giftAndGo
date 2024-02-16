package com.giftandgo.assessment.exception;

import org.springframework.http.HttpStatus;

public class RestrictedISPException extends HttpException {

    public RestrictedISPException(final String isp) {
        super(HttpStatus.FORBIDDEN, "Requests from Isp/Data center: " + isp + " are not allowed to invoke this API");
    }

}
