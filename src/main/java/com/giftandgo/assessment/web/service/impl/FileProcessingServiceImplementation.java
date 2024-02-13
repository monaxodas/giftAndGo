package com.giftandgo.assessment.web.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.web.configuration.FileProcessingConfiguration;
import com.giftandgo.assessment.web.model.OutputFileEntry;
import com.giftandgo.assessment.web.model.OutputFileResult;
import com.giftandgo.assessment.web.service.FileProcessingService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileProcessingServiceImplementation implements FileProcessingService {
    private final FileProcessingConfiguration configuration;

    private final int nameIndex = 2;
    private final int transportIndex = 3;
    private final int topSpeedIndex = 6;

    private final int expectedColumns = 6;

    public FileProcessingServiceImplementation(FileProcessingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public OutputFileResult processFile(final MultipartFile multipartFile) {
        try {
            return new OutputFileResult(new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))
                .lines()
                .filter(s -> !s.isBlank() && !s.isEmpty())
                .map(this::processLine)
                .filter(Objects::nonNull)
                .toList());
        } catch (IOException e) {
            log.error("Could not open multipart file");
            throw new RuntimeException(e);
        }
    }

    private OutputFileEntry processLine(final String line) {
        final var split = line.split(configuration.getColumnSeparator());
        if (split.length == expectedColumns) {
            return new OutputFileEntry(getNameFromLine(split), getTransportFromLine(split), getTopSpeedFromLine(split));
        } else {
            log.warn("File line is malformed {}, ignoring", line);
            return null;
        }
    }

    private String getNameFromLine(final String[] split) {
        return Optional.ofNullable(split[nameIndex]).orElse("N/A");
    }

    private String getTransportFromLine(final String[] split) {
        return Optional.ofNullable(split[transportIndex]).orElse("N/A");
    }

    private Float getTopSpeedFromLine(final String[] split) {
        return Optional.ofNullable(split[topSpeedIndex])
            .map(Float::parseFloat)
            .orElse(0F);
    }
}
