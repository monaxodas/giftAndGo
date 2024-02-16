package com.giftandgo.assessment.geolocation;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.giftandgo.assessment.exception.GeolocationException;
import com.giftandgo.assessment.model.GeolocationInfo;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GeolocationClientImpl implements GeolocationClient {
    private final GeolocationClientConfiguration configuration;

    private final RestTemplate restTemplate;

    public GeolocationClientImpl(final RestTemplate restTemplate, final GeolocationClientConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    @Override
    public GeolocationInfo getGeolocationForIpAddress(final String ipAddress) {
        try {
            log.debug("Initiating request to geolocation for ip address: {}", ipAddress);
            final String url = UriComponentsBuilder.fromHttpUrl(configuration.getUrl())
                .buildAndExpand(ipAddress)
                .toUriString();
            final var result = restTemplate.getForObject(url, GeolocationInfo.class);
            log.debug("Geolocation result: {}", result);
            return result;
        } catch (final Exception e) {
            log.error("Could not fetch geolocation info for ip address {}", ipAddress, e);
            throw new GeolocationException(e.getMessage());
        }

    }
}
