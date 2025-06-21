//package com.tomato.tomato.api.controller;
//import com.tomato.tomato.api.service.FileService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api")
//@AllArgsConstructor
//public class FileController {
//
//    private final FileService fileService;
//
//    @PostMapping("/upload")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
//        try {
//            return fileService.handleFileUpload(file);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
//        }
//    }
//}
