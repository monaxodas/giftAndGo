package com.giftandgo.assessment.assessment.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.configuration.FileProcessingConfiguration;
import com.giftandgo.assessment.exception.RestrictedCountryException;
import com.giftandgo.assessment.exception.ValidationError;
import com.giftandgo.assessment.geolocation.GeolocationClient;
import com.giftandgo.assessment.model.GeolocationInfo;
import com.giftandgo.assessment.service.FileProcessingService;
import com.giftandgo.assessment.service.ValidationService;
import com.giftandgo.assessment.service.impl.FileProcessingServiceImplementation;
import com.giftandgo.assessment.service.impl.ValidationServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;

class FileProcessingTest {
    private ValidationService validationService;

    private FileProcessingService subject;
    private FileProcessingConfiguration fileProcessingConfiguration;

    private GeolocationClient geolocationClient;
    private final String ipAddress = "testIp";

    @BeforeEach
    void setup() {
        geolocationClient = mock(GeolocationClient.class);
        fileProcessingConfiguration = new FileProcessingConfiguration();
        validationService = new ValidationServiceImpl(fileProcessingConfiguration);
        subject = new FileProcessingServiceImplementation(validationService, geolocationClient);
    }

    @Test
    void testHappyPath() {
        final var geolocationInfo = getGeolocationInfo("country", "isp");
        when(geolocationClient.getGeolocationForIpAddress(ipAddress))
            .thenReturn(geolocationInfo);
        final var validMultipartFile = getMultipartFile("InputFile.txt");
        final var parsedFile = subject.parseFile(validMultipartFile, getRequest());
        assertThat(parsedFile.outputFileEntries().size()).isEqualTo(3);
        final var firstEntry = parsedFile.outputFileEntries().get(0);
        assertThat(firstEntry.name()).isEqualTo("John Smith");
        assertThat(firstEntry.transport()).isEqualTo("Rides A Bike");
        assertThat(firstEntry.topSpeed()).isEqualTo(Double.parseDouble("12.1"));
        final var secondEntry = parsedFile.outputFileEntries().get(1);
        assertThat(secondEntry.name()).isEqualTo("Mike Smith");
        assertThat(secondEntry.transport()).isEqualTo("Drives an SUV");
        assertThat(secondEntry.topSpeed()).isEqualTo(Double.parseDouble("95.5"));
        final var thirdEntry = parsedFile.outputFileEntries().get(2);
        assertThat(thirdEntry.name()).isEqualTo("Jenny Walters");
        assertThat(thirdEntry.transport()).isEqualTo("Rides A Scooter");
        assertThat(thirdEntry.topSpeed()).isEqualTo(Double.parseDouble("15.3"));

        assertThat(parsedFile.geolocationInfo()).isEqualTo(geolocationInfo);
    }

    @Test
    void testInvalidGeolocation() {
        final var validMultipartFile = getMultipartFile("InputFile.txt");
        final var geolocationInfo = getGeolocationInfo("Spain", "isp");
        when(geolocationClient.getGeolocationForIpAddress(ipAddress))
            .thenReturn(geolocationInfo);
        fileProcessingConfiguration.setIpCountriesBlockList(List.of("Spain"));
        assertThrows(RestrictedCountryException.class, ()-> subject.parseFile(validMultipartFile, getRequest())) ;
    }

    @Test
    @SneakyThrows
    void testInvalidFile() {
        final var multipartFile = getMultipartFile("InvalidFile.txt");
        final var geolocationInfo = getGeolocationInfo("Spain", "isp");
        when(geolocationClient.getGeolocationForIpAddress(ipAddress))
            .thenReturn(geolocationInfo);
        assertThrows(ValidationError.class, ()-> subject.parseFile(multipartFile, getRequest())) ;
    }

    @SneakyThrows
    private MultipartFile getMultipartFile(final String filename) {
        final ClassPathResource resource = new ClassPathResource(filename);
       return new MockMultipartFile(filename, resource.getInputStream());
    }

    private GeolocationInfo getGeolocationInfo(final String country,final String isp) {
        return new GeolocationInfo("ipAddress", "status", country, "countryCode", "region",
            "regionName", "city", "zip", "lat", "lon", "timezone", isp, "org", "as");
    }

    private HttpServletRequest getRequest() {
        final var mock = mock(HttpServletRequest.class);
        when(mock.getRequestURI()).thenReturn("test");
        when(mock.getRemoteAddr()).thenReturn(ipAddress);
        return mock;
    }
}
