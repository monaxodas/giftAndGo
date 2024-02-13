package com.giftandgo.assessment.web.service;

import jakarta.servlet.http.HttpServletRequest;

public interface ValidationService {

    void validateRequest(HttpServletRequest request);
}
