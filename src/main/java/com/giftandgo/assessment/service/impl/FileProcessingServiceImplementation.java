package com.giftandgo.assessment.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.exception.ParseFileException;
import com.giftandgo.assessment.geolocation.GeolocationClient;
import com.giftandgo.assessment.model.InputFileEntry;
import com.giftandgo.assessment.model.OutputFileEntry;
import com.giftandgo.assessment.model.ParsedFile;
import com.giftandgo.assessment.service.FileProcessingService;
import com.giftandgo.assessment.service.ValidationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileProcessingServiceImplementation implements FileProcessingService {
    private final ValidationService validationService;
    private final GeolocationClient geolocationClient;

    public FileProcessingServiceImplementation(ValidationService validationService, GeolocationClient geolocationClient) {
        this.validationService = validationService;
        this.geolocationClient = geolocationClient;
    }

    @Override
    public ParsedFile parseFile(final MultipartFile multipartFile, final HttpServletRequest request) {

        final var geolocationInfo = geolocationClient.getGeolocationForIpAddress(request.getRemoteAddr());
        validationService.validateCountryAndIsp(geolocationInfo.country(), geolocationInfo.isp());
        final var fileLines = parseFileLines(multipartFile);
        final var inputFileEntries = validationService.validateInputFileContent(fileLines);
        return new ParsedFile(inputFileEntries.stream().map(this::convert).toList(), geolocationInfo);

    }

    private OutputFileEntry convert(final InputFileEntry inputFileEntry) {
        return new OutputFileEntry(inputFileEntry.name(), inputFileEntry.transport(), inputFileEntry.topSpeed());
    }

    private List<String> parseFileLines(final MultipartFile file) {
        try {

            return new BufferedReader(new InputStreamReader(file.getInputStream()))
                .lines()
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank() && !s.isEmpty())
                .toList();
        } catch (IOException e) {
            log.error("Could not parse multipart file");
            throw new ParseFileException(e);
        }
    }

}
