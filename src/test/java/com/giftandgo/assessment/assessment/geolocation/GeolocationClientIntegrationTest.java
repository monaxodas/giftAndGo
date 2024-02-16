package com.giftandgo.assessment.assessment.geolocation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.giftandgo.assessment.exception.GeolocationException;
import com.giftandgo.assessment.geolocation.GeolocationClient;
import com.giftandgo.assessment.geolocation.GeolocationClientConfiguration;
import com.giftandgo.assessment.geolocation.GeolocationClientImpl;
import com.giftandgo.assessment.model.GeolocationInfo;

public class GeolocationClientIntegrationTest {

    private GeolocationClientConfiguration configuration = new GeolocationClientConfiguration();
    private RestTemplate restTemplate;
    private GeolocationClient client;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);
        client = new GeolocationClientImpl(restTemplate, configuration);
    }

    @Test
    void testClientThrowsError() {
        final var ipAddress = "ipAddress";
        final var url = "http://ip-api.com/json/ipAddress";
        when(restTemplate.getForObject(url, GeolocationInfo.class))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(GeolocationException.class, () -> client.getGeolocationForIpAddress(ipAddress));
        verify(restTemplate, times(1)).getForObject(url, GeolocationInfo.class);
    }

    @Test
    void testHappyPath() {
        final var ipAddress = "ipAddress";
        final var url = "http://ip-api.com/json/ipAddress";
        when(restTemplate.getForObject(url, GeolocationInfo.class))
            .thenReturn(getGeolocationInfo());

        final var geolocationInfo = client.getGeolocationForIpAddress(ipAddress);

        assertThat(geolocationInfo).isEqualTo(getGeolocationInfo());

        verify(restTemplate, times(1)).getForObject(url, GeolocationInfo.class);
    }


    private GeolocationInfo getGeolocationInfo() {
        return new GeolocationInfo("ipAddress", "status", "country", "countryCode", "region",
            "regionName", "city", "zip", "lat", "lon", "timezone", "isp", "org", "as");
    }

}
