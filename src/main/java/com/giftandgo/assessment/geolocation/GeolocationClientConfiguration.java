package com.giftandgo.assessment.geolocation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties("geolocation.client")
@Data
public class GeolocationClientConfiguration {
    private String url= "http://ip-api.com/json/{query}";
}
