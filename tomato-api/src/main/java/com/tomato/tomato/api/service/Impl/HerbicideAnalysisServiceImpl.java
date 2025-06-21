package com.tomato.tomato.api.service.Impl;

import com.tomato.tomato.api.controller.response.HerbicideAnalysisResponse;
import com.tomato.tomato.api.service.FileService;
import com.tomato.tomato.api.model.Herbicide;
import com.tomato.tomato.api.model.HerbicideAnalysisRecord;
import com.tomato.tomato.api.repository.HerbicideRepository;
import com.tomato.tomato.api.repository.HerbicideAnalysisRecordRepository;
import com.tomato.tomato.api.service.HerbicideAnalysisService;
import com.tomato.tomato.api.controller.WeatherController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HerbicideAnalysisServiceImpl implements HerbicideAnalysisService {

    private final FileService fileService;
    private final WeatherController weatherController;
    private final HerbicideRepository herbicideRepository;
    private final HerbicideAnalysisRecordRepository analysisRecordRepository;
    private final ObjectMapper objectMapper;

    // Endpoint of your ML model for prediction
//    private static final String ML_API_URL = "http://localhost:5001/predict";

    // New YOLOv8x model endpoint
//    private static final String YOLO_API_URL = "http://localhost:6000/predict";

    // Directory to save uploaded images
//    private static final String UPLOAD_DIR = "uploads/herbicide-analysis/";

    @Value("${ml.model.api.url}")
    private String ML_API_URL;

    @Value("${file.upload-dir}")
    private String UPLOAD_DIR;

    @Value("${yolo.api-url}")
    private String YOLO_API_URL;


    @Override
    public ResponseEntity<HerbicideAnalysisResponse> analyzeHerbicide(MultipartFile image, String soilType, String growthStage, Double temperature, Double humidity, Double latitude, Double longitude, Boolean useNewModel) throws IOException {

        HerbicideAnalysisRecord record = new HerbicideAnalysisRecord();
        String imagePath = null;

        try {
            // Save the uploaded image
            imagePath = saveImage(image);
            record.setImagePath(imagePath);

            String weedName;
            double confidence;
            HerbicideAnalysisResponse.DetectionInfo detectionInfo = null;

            if (useNewModel) {
                // Use new YOLOv8x model
                var weedDetectionResult = callYoloModel(image);
                weedName = weedDetectionResult.getWeedName();
                confidence = weedDetectionResult.getConfidence();
                detectionInfo = weedDetectionResult.getDetectionInfo();
                record.setModelUsed("YOLOv8x");
            } else {
                // Use existing VGG16 model via FileService
                ResponseEntity<String> flaskResponse = fileService.handleFileUpload(image);
                String responseBody = flaskResponse.getBody();

                // Parse weed data from Flask response
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                weedName = jsonNode.has("weed_type") ? jsonNode.get("weed_type").asText() : "Unknown";
                confidence = jsonNode.has("confidence") ? jsonNode.get("confidence").asDouble() * 100 : 0.0; // Convert to %

                // Create detection info for VGG16 model (with default bounding box)
                double defaultImageWidth = 640.0;
                double defaultImageHeight = 480.0;

                HerbicideAnalysisResponse.BoundingBox defaultBbox = createDefaultBoundingBox(defaultImageWidth, defaultImageHeight);

                detectionInfo = new HerbicideAnalysisResponse.DetectionInfo(
                        1, // Single detection
                        List.of(new HerbicideAnalysisResponse.Detection(
                                defaultBbox,
                                0,
                                weedName,
                                confidence / 100
                        )),
                        "VGG16"
                );
                record.setModelUsed("VGG16");
            }

            // Set basic analysis data in record
            record.setWeedName(weedName);
            record.setConfidence(confidence);
            record.setSoilType(soilType);
            record.setGrowthStage(growthStage);
            record.setTemperature(temperature);
            record.setHumidity(humidity);
            record.setLatitude(latitude);
            record.setLongitude(longitude);
            record.setDetectionCount(detectionInfo != null ? detectionInfo.getDetectionCount() : 1);

            // 2. Get wind speed and rainfall from Weather API
            Double windSpeed = null;
            Double rainfall = 0.0;
            if (latitude != null && longitude != null) {
                Map<String, Object> weatherData = weatherController.getWeatherFromCoordinates(latitude, longitude);
                windSpeed = weatherData.containsKey("wind_speed") ? (Double) weatherData.get("wind_speed") : null;
                rainfall = weatherData.containsKey("rainfall") ? (Double) weatherData.get("rainfall") : 0.0;
            }

            record.setWindSpeed(windSpeed);
            record.setRainfall(rainfall);

            // Generate weather constraints
            String weatherConstraints = getWeatherConstraints(windSpeed, rainfall);
            record.setWeatherConstraints(weatherConstraints);

            // 3. Prepare data for the ML model API
            Map<String, Object> mlRequestBody = Map.of(
                    "Weed Name", weedName,
                    "Soil Type", soilType,
                    "Growth Stage", growthStage,
                    "Temp (°C)", temperature,
                    "Humidity (%)", humidity,
                    "Wind Speed (km/h)", windSpeed,
                    "Rainfall (mm)", rainfall
            );

            // Send request to ML model
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> mlResponse = restTemplate.postForEntity(ML_API_URL, mlRequestBody, Map.class);

            // Extract predictions from the ML model response
            Double predictedApplicationRate = null;
            String predictedHerbicideName = null;
            if (mlResponse.getBody() != null) {
                predictedApplicationRate = (Double) mlResponse.getBody().get("Predicted Application Rate (L/ha)");
                predictedHerbicideName = (String) mlResponse.getBody().get("Predicted Herbicide Name");
            }

            record.setPredictedHerbicideName(predictedHerbicideName);
            record.setPredictedApplicationRate(predictedApplicationRate);

            // 4. Create response object with all relevant data
            HerbicideAnalysisResponse response = new HerbicideAnalysisResponse();
            response.setWeedName(weedName);
            response.setConfidence(confidence);
            response.setSoilType(soilType);
            response.setGrowthStage(growthStage);
            response.setTemperature(temperature);
            response.setHumidity(humidity);
            response.setRainfall(rainfall);
            response.setWindSpeed(windSpeed);
            response.setPredictedApplicationRate(predictedApplicationRate);
            response.setPredictedHerbicideName(predictedHerbicideName);
            response.setDetectionInfo(detectionInfo);

            // 5. Fetch additional herbicide details from database
            List<HerbicideAnalysisResponse.HerbicideOption> herbicideOptions = new ArrayList<>();
            if (predictedHerbicideName != null) {
                Optional<Herbicide> herbicideOptional = herbicideRepository.findByName(predictedHerbicideName);

                if (herbicideOptional.isPresent()) {
                    Herbicide herbicide = herbicideOptional.get();

                    HerbicideAnalysisResponse.HerbicideOption option = new HerbicideAnalysisResponse.HerbicideOption(
                            herbicide.getName(),
                            predictedApplicationRate + " L/ha",
                            herbicide.getSafeForTomato() != null ? herbicide.getSafeForTomato().toString() : "Unknown",
                            herbicide.getModeOfAction(),
                            herbicide.getApplicationMethod(),
                            weatherConstraints,
                            herbicide.getResistanceReported() != null ? herbicide.getResistanceReported().toString() : "Unknown",
                            herbicide.getAlternativeHerbicide()
                    );

                    herbicideOptions.add(option);

                    // Add safety precautions
                    HerbicideAnalysisResponse.SafetyPrecautions safetyPrecautions = new HerbicideAnalysisResponse.SafetyPrecautions(
                            herbicide.getToxicity(),
                            herbicide.getHumanProtection(),
                            herbicide.getEnvironmentalPrecautions()
                    );

                    response.setSafetyPrecautions(safetyPrecautions);

                    // Save safety precautions as JSON string in record
                    record.setSafetyPrecautions(objectMapper.writeValueAsString(safetyPrecautions));

                    // Handle alternative herbicide
                    if (herbicide.getAlternativeHerbicide() != null && !herbicide.getAlternativeHerbicide().isEmpty()) {
                        Optional<Herbicide> alternativeHerbicideOptional = herbicideRepository.findByName(herbicide.getAlternativeHerbicide());

                        if (alternativeHerbicideOptional.isPresent()) {
                            Herbicide alternativeHerbicide = alternativeHerbicideOptional.get();

                            HerbicideAnalysisResponse.HerbicideOption altOption = new HerbicideAnalysisResponse.HerbicideOption(
                                    alternativeHerbicide.getName(),
                                    (predictedApplicationRate * 0.9) + " L/ha",
                                    alternativeHerbicide.getSafeForTomato() != null ? alternativeHerbicide.getSafeForTomato().toString() : "Unknown",
                                    alternativeHerbicide.getModeOfAction(),
                                    alternativeHerbicide.getApplicationMethod(),
                                    weatherConstraints,
                                    alternativeHerbicide.getResistanceReported() != null ? alternativeHerbicide.getResistanceReported().toString() : "Unknown",
                                    alternativeHerbicide.getAlternativeHerbicide()
                            );

                            herbicideOptions.add(altOption);
                        }
                    }
                }
            }

            response.setHerbicideOptions(herbicideOptions);

            // Save herbicide options as JSON string in record
            record.setHerbicideOptions(objectMapper.writeValueAsString(herbicideOptions));
            record.setAnalysisStatus("SUCCESS");

            // Save the record to database
            analysisRecordRepository.save(record);

            log.info("Herbicide analysis completed successfully for weed: {} with confidence: {}%", weedName, confidence);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during herbicide analysis", e);

            // Save error record
            record.setAnalysisStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            analysisRecordRepository.save(record);

            throw e;
        }
    }

    /**
     * Save uploaded image to local storage
     */
    private String saveImage(MultipartFile image) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    // History management methods
    @Override
    public List<HerbicideAnalysisRecord> getAllAnalysisHistory() {
        return analysisRecordRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<HerbicideAnalysisRecord> getAnalysisHistoryByUser(String userId) {
        return analysisRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<HerbicideAnalysisRecord> getAnalysisHistoryPaginated(Pageable pageable) {
        return analysisRecordRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<HerbicideAnalysisRecord> getAnalysisHistoryByUserPaginated(String userId, Pageable pageable) {
        return analysisRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public List<HerbicideAnalysisRecord> getAnalysisHistoryByWeed(String weedName) {
        return analysisRecordRepository.findByWeedNameIgnoreCaseOrderByCreatedAtDesc(weedName);
    }

    @Override
    public List<HerbicideAnalysisRecord> getAnalysisHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analysisRecordRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<HerbicideAnalysisRecord> getRecentAnalysisHistory(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return analysisRecordRepository.findRecentRecords(cutoffDate);
    }

    @Override
    public HerbicideAnalysisRecord getAnalysisRecordById(Long id) {
        return analysisRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Analysis record not found with id: " + id));
    }

    @Override
    public void deleteAnalysisRecord(Long id) {
        HerbicideAnalysisRecord record = getAnalysisRecordById(id);

        // Delete associated image file if exists
        if (record.getImagePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(record.getImagePath()));
            } catch (IOException e) {
                log.warn("Failed to delete image file: {}", record.getImagePath(), e);
            }
        }

        analysisRecordRepository.deleteById(id);
    }

    @Override
    public long getTotalAnalysisCount() {
        return analysisRecordRepository.count();
    }

    @Override
    public long getAnalysisCountByUser(String userId) {
        return analysisRecordRepository.countByUserId(userId);
    }

    // Existing helper methods remain the same
    private HerbicideAnalysisResponse.BoundingBox createDefaultBoundingBox(double imageWidth, double imageHeight) {
        double boxWidth = imageWidth * 0.4;
        double boxHeight = imageHeight * 0.4;

        double centerX = imageWidth / 2;
        double centerY = imageHeight / 2;

        double x1 = centerX - (boxWidth / 2);
        double x2 = centerX + (boxWidth / 2);
        double y1 = centerY - (boxHeight / 2);
        double y2 = centerY + (boxHeight / 2);

        return new HerbicideAnalysisResponse.BoundingBox(
                boxHeight, boxWidth, x1, x2, y1, y2
        );
    }

    private WeedDetectionResult callYoloModel(MultipartFile image) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", image.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(YOLO_API_URL, requestEntity, Map.class);

        Map responseBody = response.getBody();
        String weedName = "Unknown";
        double confidence = 0.0;
        HerbicideAnalysisResponse.DetectionInfo detectionInfo = null;

        if (responseBody != null && "success".equals(responseBody.get("status"))) {
            Integer detectionCount = (Integer) responseBody.get("detection_count");
            List<Map<String, Object>> detectionsData = (List<Map<String, Object>>) responseBody.get("detections");

            List<HerbicideAnalysisResponse.Detection> detections = new ArrayList<>();

            if (detectionsData != null && !detectionsData.isEmpty()) {
                for (Map<String, Object> detectionData : detectionsData) {
                    Map<String, Object> bboxData = (Map<String, Object>) detectionData.get("bbox");
                    HerbicideAnalysisResponse.BoundingBox bbox = null;

                    if (bboxData != null) {
                        bbox = new HerbicideAnalysisResponse.BoundingBox(
                                ((Number) bboxData.get("height")).doubleValue(),
                                ((Number) bboxData.get("width")).doubleValue(),
                                ((Number) bboxData.get("x1")).doubleValue(),
                                ((Number) bboxData.get("x2")).doubleValue(),
                                ((Number) bboxData.get("y1")).doubleValue(),
                                ((Number) bboxData.get("y2")).doubleValue()
                        );
                    }

                    Integer classId = (Integer) detectionData.get("class_id");
                    String className = (String) detectionData.get("class_name");
                    Double detectionConfidence = null;

                    Object confidenceObj = detectionData.get("confidence");
                    if (confidenceObj instanceof Double) {
                        detectionConfidence = (Double) confidenceObj;
                    } else if (confidenceObj instanceof Float) {
                        detectionConfidence = ((Float) confidenceObj).doubleValue();
                    }

                    HerbicideAnalysisResponse.Detection detection = new HerbicideAnalysisResponse.Detection(
                            bbox, classId, className, detectionConfidence
                    );

                    detections.add(detection);
                }

                HerbicideAnalysisResponse.Detection firstDetection = detections.getFirst();
                weedName = firstDetection.getClassName();
                confidence = firstDetection.getConfidence() * 100;
            }

            detectionInfo = new HerbicideAnalysisResponse.DetectionInfo(detectionCount, detections, "YOLOv8x");
        }

        return new WeedDetectionResult(weedName, confidence, detectionInfo);
    }

    private String getWeatherConstraints(Double windSpeed, Double rainfall) {
        StringBuilder constraints = new StringBuilder();

        if (windSpeed != null) {
            if (windSpeed > 10) {
                constraints.append("High wind, not recommended");
            } else if (windSpeed > 5) {
                constraints.append("Moderate wind, use caution");
            } else {
                constraints.append("Low wind, favorable");
            }
        }

        if (rainfall > 0) {
            if (!constraints.isEmpty()) {
                constraints.append(", ");
            }

            if (rainfall > 15) {
                constraints.append("Heavy rain, avoid application");
            } else if (rainfall > 5) {
                constraints.append("Light rain, limited efficacy");
            } else {
                constraints.append("No significant rain");
            }
        }

        return !constraints.isEmpty() ? constraints.toString() : "No specific constraints";
    }

    @Getter
    private static class WeedDetectionResult {
        private final String weedName;
        private final double confidence;
        private final HerbicideAnalysisResponse.DetectionInfo detectionInfo;

        public WeedDetectionResult(String weedName, double confidence, HerbicideAnalysisResponse.DetectionInfo detectionInfo) {
            this.weedName = weedName;
            this.confidence = confidence;
            this.detectionInfo = detectionInfo;
        }

    }
}