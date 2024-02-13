package com.giftandgo.assessment.web.service.impl;

import org.springframework.stereotype.Service;

import com.giftandgo.assessment.web.service.ValidationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidationServiceImplementation implements ValidationService {
    @Override
    public void validateRequest(final HttpServletRequest request) {
    log.info("request: {}", request);
    }
}
