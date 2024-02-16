package com.giftandgo.assessment.web;

import java.time.Instant;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.exception.HttpException;
import com.giftandgo.assessment.geolocation.GeolocationClient;
import com.giftandgo.assessment.model.ParsedFile;
import com.giftandgo.assessment.service.FileProcessingService;
import com.giftandgo.assessment.service.RequestLoggingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class FileController {
    private final FileProcessingService fileProcessingService;
    private final RequestLoggingService requestLoggingService;
    private final GeolocationClient geolocationClient;


    public FileController(final FileProcessingService fileProcessingService,
        final RequestLoggingService requestLoggingService,
        final GeolocationClient geolocationClient) {
        this.fileProcessingService = fileProcessingService;
        this.requestLoggingService = requestLoggingService;
        this.geolocationClient = geolocationClient;
    }

    @PostMapping(path = "/file/process", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> processFile(@RequestParam(value = "file", required = false) final MultipartFile file,
        final HttpServletRequest request) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final var timestamp = Instant.now();
        HttpStatus status = HttpStatus.OK;
        String errorMessage = null;
        ParsedFile result = null;
        try {
            result = fileProcessingService.parseFile(file, request);
        } catch (HttpException e) {
            status = e.getStatus();
            errorMessage = e.getMessage();
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = e.getMessage();
        } finally {
            stopWatch.stop();
            requestLoggingService.logRequest(request, stopWatch.getTotalTimeMillis(), timestamp,
                status, Optional.ofNullable(result).map(ParsedFile::geolocationInfo).orElse(null));
        }
        if (errorMessage != null) {
            return ResponseEntity.status(status).body(errorMessage);
        }
        return ResponseEntity.ok(result);
    }

}
