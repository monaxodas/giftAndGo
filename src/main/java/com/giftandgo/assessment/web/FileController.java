package com.giftandgo.assessment.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.web.model.OutputFileResult;
import com.giftandgo.assessment.web.service.FileProcessingService;
import com.giftandgo.assessment.web.service.ValidationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class FileController {
    private final ValidationService validationService;
    private final FileProcessingService fileProcessingService;

    public FileController(final ValidationService validationService, final FileProcessingService fileProcessingService) {
        this.validationService = validationService;
        this.fileProcessingService = fileProcessingService;
    }

    @PostMapping(path =  "/file/process",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<OutputFileResult> processFile(@RequestParam("file") MultipartFile file,
        final HttpServletRequest request) {
        validationService.validateRequest(request);
        return ResponseEntity.ok(fileProcessingService.processFile(file));
    }

}
