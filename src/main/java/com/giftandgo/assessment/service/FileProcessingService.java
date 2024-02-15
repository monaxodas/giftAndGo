package com.giftandgo.assessment.service;

import org.springframework.web.multipart.MultipartFile;

import com.giftandgo.assessment.model.GeolocationInfo;
import com.giftandgo.assessment.model.ParsedFile;

public interface FileProcessingService {

    ParsedFile parseFile(MultipartFile multipartFile, GeolocationInfo geolocationInfo);
}
