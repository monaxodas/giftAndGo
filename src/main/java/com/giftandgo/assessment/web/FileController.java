package com.giftandgo.assessment.web;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.exception.RestrictedCountryException;
import com.giftandgo.assessment.exception.RestrictedISPException;
import com.giftandgo.assessment.exception.ValidationError;
import com.giftandgo.assessment.geolocation.GeolocationClient;
import com.giftandgo.assessment.jpa.RequestLogEntity;
import com.giftandgo.assessment.jpa.RequestLogEntityRepository;
import com.giftandgo.assessment.model.GeolocationInfo;
import com.giftandgo.assessment.model.ParsedFile;
import com.giftandgo.assessment.service.FileProcessingService;
import com.giftandgo.assessment.service.ValidationService;
import com.giftandgo.assessment.service.RequestLoggingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class FileController {
    private final FileProcessingService fileProcessingService;
    private final RequestLoggingService requestLoggingService;
    private final GeolocationClient geolocationClient;
    private final RequestLogEntityRepository repository;


    public FileController(final FileProcessingService fileProcessingService,
        final RequestLoggingService requestLoggingService,
        final GeolocationClient geolocationClient,
        final RequestLogEntityRepository repository) {
        this.fileProcessingService = fileProcessingService;
        this.requestLoggingService = requestLoggingService;
        this.geolocationClient = geolocationClient;
        this.repository = repository;
    }

    @PostMapping(path = "/file/process", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> processFile(@RequestParam("file") MultipartFile file,
        final HttpServletRequest request) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final var timestamp = Instant.now();
        HttpStatus status = HttpStatus.OK;
        GeolocationInfo geolocationInfo = null;
        String errorMessage = null;
        ParsedFile result = null;
        try {
            geolocationInfo = geolocationClient.getGeolocationForIpAddress(request.getRemoteAddr());
            result = fileProcessingService.parseFile(file, geolocationInfo);
        } catch (ValidationError e) {
            status = HttpStatus.BAD_REQUEST;
            errorMessage = e.getErrorMessage();
        } catch (RestrictedCountryException | RestrictedISPException exception) {
            status = HttpStatus.FORBIDDEN;
            errorMessage = exception.getMessage();
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = e.getMessage();
        } finally {
            stopWatch.stop();
            requestLoggingService.logRequest(request, stopWatch.getTotalTimeMillis(), timestamp,
                status, geolocationInfo);
        }
        if (errorMessage != null) {
            return ResponseEntity.status(status).body(errorMessage);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<RequestLogEntity>> getAllEntities() {
        return ResponseEntity.ok(repository.findAll());
    }

}
