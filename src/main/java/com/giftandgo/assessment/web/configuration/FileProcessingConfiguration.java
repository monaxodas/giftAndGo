package com.giftandgo.assessment.web.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.giftandgo.assessment.web.model.FileColumns;

import lombok.Data;

@Configuration
@ConfigurationProperties("file.configuration")
@Data
public class FileProcessingConfiguration {
    private List<String> outputColumns = new ArrayList<>();
    private String columnSeparator = "\\|";


    public Set<FileColumns> getOutputColumns() {
        return outputColumns.stream().map(FileColumns::valueOf).collect(Collectors.toSet());
    }
}
