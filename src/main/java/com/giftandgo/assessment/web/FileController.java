package com.giftandgo.assessment.web;

import static com.giftandgo.assessment.web.FileController.PATH;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(PATH)
@RequiredArgsConstructor
public class FileController {
    static final String PATH = "/file";
    static final String PROCESS_PATH = PATH + "/process";

    @PostMapping(path = PROCESS_PATH)
    public ResponseEntity<?> processFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok("ok");
    }

}
