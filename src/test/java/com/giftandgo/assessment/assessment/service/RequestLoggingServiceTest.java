package com.giftandgo.assessment.assessment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;

import com.giftandgo.assessment.jpa.RequestLogEntityRepository;
import com.giftandgo.assessment.model.GeolocationInfo;
import com.giftandgo.assessment.service.RequestLoggingService;

import jakarta.servlet.http.HttpServletRequest;

@DataJpaTest
class RequestLoggingServiceTest {

    @Autowired
    private RequestLogEntityRepository repository;

    private RequestLoggingService subject;

    @BeforeEach
    void setup() {
        subject = new RequestLoggingService(repository);
    }

    @Test
    void testHappyPath_withGeolocationInfo() {
        final var ipAddress = "0.0.0.0";
        final var uri = "http://www.example.com/";
        final var timeElapsed = 10000;
        final var now = Instant.now();
        final var status = HttpStatus.OK;
        final var countryCode = "FRA";
        final var isp = "AZURE";

        final var geolocationInfo = getGeolocationInfo(ipAddress, countryCode, isp);
        final var request = getRequest(uri, ipAddress);
        subject.logRequest(request, timeElapsed, now, status, geolocationInfo);

        final var entities = repository.findAll();

        assertThat(entities.size()).isEqualTo(1);

        final var logEntity = entities.get(0);
        assertThat(logEntity.getUri()).isEqualTo(uri);
        assertThat(logEntity.getTimestamp()).isEqualTo(now);
        assertThat(logEntity.getResponseCode()).isEqualTo(status);
        assertThat(logEntity.getIpAddress()).isEqualTo(ipAddress);
        assertThat(logEntity.getCountryCode()).isEqualTo(countryCode);
        assertThat(logEntity.getIsp()).isEqualTo(isp);
        assertThat(logEntity.getTimeElapsed()).isEqualTo(timeElapsed);
        assertThat(logEntity.getCreatedDate()).isNotNull();
    }

    @Test
    void testHappyPath_withoutGeolocationInfo() {
        final var ipAddress = "0.0.0.0";
        final var uri = "http://www.example.com/";
        final var timeElapsed = 10000;
        final var now = Instant.now();
        final var status = HttpStatus.BAD_REQUEST;

        final var request = getRequest(uri, ipAddress);
        subject.logRequest(request, timeElapsed, now, status, null);

        final var entities = repository.findAll();

        assertThat(entities.size()).isEqualTo(1);

        final var logEntity = entities.get(0);
        assertThat(logEntity.getUri()).isEqualTo(uri);
        assertThat(logEntity.getTimestamp()).isEqualTo(now);
        assertThat(logEntity.getResponseCode()).isEqualTo(status);
        assertThat(logEntity.getIpAddress()).isEqualTo(ipAddress);
        assertThat(logEntity.getCountryCode()).isNull();
        assertThat(logEntity.getIsp()).isNull();
        assertThat(logEntity.getTimeElapsed()).isEqualTo(timeElapsed);
        assertThat(logEntity.getCreatedDate()).isNotNull();
    }

    private HttpServletRequest getRequest(final String uri, final String ipAddress) {
        final var mock = mock(HttpServletRequest.class);
        when(mock.getRequestURI()).thenReturn(uri);
        when(mock.getRemoteAddr()).thenReturn(ipAddress);
        return mock;
    }

    private GeolocationInfo getGeolocationInfo(final String ipAddress, final String countryCode, final String isp) {
        return new GeolocationInfo(ipAddress, "status", "country", countryCode, "region",
            "regionName", "city", "zip", "lat", "lon", "timezone", isp, "org", "as");
    }

}
