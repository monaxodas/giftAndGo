package com.giftandgo.assessment.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.giftandgo.assessment.model.ParsedColumn;

import lombok.Data;

@Configuration
@ConfigurationProperties("file.configuration")
@Data
public class FileProcessingConfiguration {
    private List<String> outputColumns = new ArrayList<>();
    private String columnSeparator = "\\|";
    private List<String> ipCountriesBlockList = new ArrayList<>();
    private List<String> ipDataCentersBlockList = new ArrayList<>();
    private Boolean validationEnabled = true;


    public Set<ParsedColumn> getOutputColumns() {
        return outputColumns.stream().map(ParsedColumn::valueOf).collect(Collectors.toSet());
    }
}
