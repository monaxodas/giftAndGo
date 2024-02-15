package com.giftandgo.assessment.service.impl;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.exception.ParseFileException;
import com.giftandgo.assessment.model.GeolocationInfo;
import com.giftandgo.assessment.model.InputFileEntry;
import com.giftandgo.assessment.service.FileProcessingService;
import com.giftandgo.assessment.configuration.FileProcessingConfiguration;
import com.giftandgo.assessment.model.ParsedFile;
import com.giftandgo.assessment.model.OutputFileEntry;
import com.giftandgo.assessment.model.OutputFileResult;
import com.giftandgo.assessment.service.ValidationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileProcessingServiceImplementation implements FileProcessingService {
    private final ValidationService validationService;

    public FileProcessingServiceImplementation(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public ParsedFile parseFile(final MultipartFile multipartFile, final GeolocationInfo geolocationInfo) {
        validationService.validateCountryAndIsp(geolocationInfo.country(), geolocationInfo.isp());
        try {
            final var fileLines = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))
                .lines()
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank() && !s.isEmpty())
                .toList();

            final var outputFileEntries = validationService.validateInputFileContent(fileLines)
                .stream()
                .map(this::convert)
                .toList();

            return new ParsedFile(outputFileEntries);
        } catch (IOException e) {
            log.error("Could not parse multipart file");
            throw new ParseFileException(e);
        }
    }

    private OutputFileEntry convert(final InputFileEntry inputFileEntry) {
        return new OutputFileEntry(inputFileEntry.name(), inputFileEntry.transport(), inputFileEntry.topSpeed());
    }

}
