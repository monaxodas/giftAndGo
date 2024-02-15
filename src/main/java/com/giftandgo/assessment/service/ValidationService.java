package com.giftandgo.assessment.service;

import java.util.List;

import com.giftandgo.assessment.model.InputFileEntry;

public interface ValidationService {

    void validateCountryAndIsp(String country, String isp);

    List<InputFileEntry> validateInputFileContent(List<String> fileLines);
}
