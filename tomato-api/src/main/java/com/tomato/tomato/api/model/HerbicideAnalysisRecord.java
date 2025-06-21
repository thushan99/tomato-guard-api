package com.tomato.tomato.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "herbicide_analysis_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HerbicideAnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weed_name")
    private String weedName;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "soil_type")
    private String soilType;

    @Column(name = "growth_stage")
    private String growthStage;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "rainfall")
    private Double rainfall;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "predicted_herbicide_name")
    private String predictedHerbicideName;

    @Column(name = "predicted_application_rate")
    private Double predictedApplicationRate;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "detection_count")
    private Integer detectionCount;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "weather_constraints")
    private String weatherConstraints;

    @Column(name = "safety_precautions", columnDefinition = "TEXT")
    private String safetyPrecautions;

    @Column(name = "herbicide_options", columnDefinition = "TEXT")
    private String herbicideOptions; // JSON string

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id")
    private String userId; // Optional: to track which user made the analysis

    @Column(name = "analysis_status")
    private String analysisStatus; // SUCCESS, FAILED, PENDING

    @Column(name = "error_message")
    private String errorMessage;
}