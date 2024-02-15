package com.giftandgo.assessment.jpa;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.http.HttpStatus;

import com.giftandgo.assessment.model.GeolocationInfo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "request_log")
@Entity
@NoArgsConstructor
@Data
public class RequestLogEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "uri", nullable = false)
    private String uri;
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    @Column(name = "response_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private HttpStatus responseCode;
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    @Column(name = "country_code")
    private String countryCode;
    @Column(name = "isp")
    private String isp;
    @Column(name = "time_elapsed")
    private long timeElapsed;
    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    public static RequestLogEntity create(final HttpServletRequest httpServletRequest, final Instant timestamp,
        final HttpStatus responseCode,
        final long timeElapsed,
        final GeolocationInfo geolocationInfo) {
        final var requestLogEntity = new RequestLogEntity();
        requestLogEntity.setId(UUID.randomUUID());
        requestLogEntity.setUri(httpServletRequest.getRequestURI());
        requestLogEntity.setTimestamp(timestamp);
        requestLogEntity.setResponseCode(responseCode);
        requestLogEntity.setTimeElapsed(timeElapsed);
        Optional.ofNullable(geolocationInfo)
            .ifPresent(info -> {
                requestLogEntity.setCountryCode(info.countryCode());
                requestLogEntity.setIsp(info.isp());
            });
        requestLogEntity.setIpAddress(httpServletRequest.getRemoteAddr());
        requestLogEntity.setCreatedDate(Instant.now());
        return requestLogEntity;
    }

}
