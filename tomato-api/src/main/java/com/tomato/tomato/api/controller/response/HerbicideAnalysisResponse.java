package com.tomato.tomato.api.controller.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HerbicideAnalysisResponse {

    private String weedName;
    private Double confidence;
    private String soilType;
    private String growthStage;
    private Double temperature;
    private Double humidity;
    private Double rainfall;
    private Double windSpeed;
    private Double predictedApplicationRate;
    private String predictedHerbicideName;
    private List<HerbicideOption> herbicideOptions;
    private SafetyPrecautions safetyPrecautions;

    // New field for detection information (only populated when using new model)
    private DetectionInfo detectionInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HerbicideOption {
        private String name;
        private String applicationRate;
        private String safeForTomato;
        private String modeOfAction;
        private String applicationMethod;
        private String weatherConstraints;
        private String resistanceReported;
        private String alternativeHerbicide;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyPrecautions {
        private String toxicity;
        private String humanProtection;
        private String environmentalPrecautions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectionInfo {
        private Integer detectionCount;
        private List<Detection> detections;
        private String modelUsed; // "VGG16" or "YOLOv8x"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detection {
        private BoundingBox bbox;
        private Integer classId;
        private String className;
        private Double confidence;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBox {
        private Double height;
        private Double width;
        private Double x1;
        private Double x2;
        private Double y1;
        private Double y2;
    }
}