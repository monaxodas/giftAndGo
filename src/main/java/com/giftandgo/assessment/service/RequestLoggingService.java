package com.giftandgo.assessment.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.giftandgo.assessment.jpa.RequestLogEntity;
import com.giftandgo.assessment.jpa.RequestLogEntityRepository;
import com.giftandgo.assessment.model.GeolocationInfo;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
public class RequestLoggingService {

    private final RequestLogEntityRepository repository;

    public RequestLoggingService(RequestLogEntityRepository repository) {
        this.repository = repository;
    }

    public void logRequest(final HttpServletRequest request, long timeElapsed, final Instant timestamp, final HttpStatus status,
        final GeolocationInfo geolocationInfo) {
        final var requestLogEntity = RequestLogEntity.create(request, timestamp, status, timeElapsed, geolocationInfo);
        repository.save(requestLogEntity);
    }
}
