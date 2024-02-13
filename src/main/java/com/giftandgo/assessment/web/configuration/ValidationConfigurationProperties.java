package com.giftandgo.assessment.web.configuration;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties("validation")
@Data
public class ValidationConfigurationProperties {
    private Boolean enabled = true;
    private List<String> ipCountriesBlockList = new ArrayList<>();
    private List<String> ipDataCentersBlockList = new ArrayList<>();
}
