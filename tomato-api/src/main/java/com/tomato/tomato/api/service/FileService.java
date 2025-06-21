package com.tomato.tomato.api.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    ResponseEntity<String> handleFileUpload(MultipartFile file) throws IOException;
}