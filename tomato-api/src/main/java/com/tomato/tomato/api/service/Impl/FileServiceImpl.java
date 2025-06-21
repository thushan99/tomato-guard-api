package com.tomato.tomato.api.service.Impl;

import com.tomato.tomato.api.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final RestTemplate restTemplate;

    @Value("${image.model.api.url}")
    private String VGG16_API_URL;

    @Override
    public ResponseEntity<String> handleFileUpload(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("upload-", ".tmp");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());

            FileSystemResource fileResource = new FileSystemResource(tempFile);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(VGG16_API_URL, requestEntity, String.class);
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            throw new IOException("Failed to send file to Flask API", e);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
