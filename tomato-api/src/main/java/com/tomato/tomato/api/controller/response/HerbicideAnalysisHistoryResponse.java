package com.tomato.tomato.api.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HerbicideAnalysisHistoryResponse {

    private Long id;
    private String weedName;
    private Double confidence;
    private String soilType;
    private String growthStage;
    private Double temperature;
    private Double humidity;
    private Double rainfall;
    private Double windSpeed;
    private Double latitude;
    private Double longitude;
    private String predictedHerbicideName;
    private Double predictedApplicationRate;
    private String modelUsed;
    private Integer detectionCount;
    private String imagePath;
    private String weatherConstraints;
    private String analysisStatus;
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String userId;

    // Parsed JSON fields
    private SafetyPrecautions safetyPrecautions;
    private List<HerbicideOption> herbicideOptions;

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
    public static class AnalysisStatistics {
        private long totalAnalyses;
        private long successfulAnalyses;
        private long failedAnalyses;
        private long userAnalyses;
        private List<String> topWeeds;
        private List<String> topHerbicides;
        private LocalDateTime lastAnalysis;
        private LocalDateTime firstAnalysis;
    }
}