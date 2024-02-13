package com.giftandgo.assessment.web.service;

import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.web.model.OutputFileResult;

public interface FileProcessingService {

    OutputFileResult processFile(MultipartFile multipartFile);
}
