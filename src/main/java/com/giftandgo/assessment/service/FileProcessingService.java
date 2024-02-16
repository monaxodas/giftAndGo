package com.giftandgo.assessment.service;

import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.model.ParsedFile;

import jakarta.servlet.http.HttpServletRequest;

public interface FileProcessingService {

    ParsedFile parseFile(MultipartFile multipartFile, final HttpServletRequest request);
}
