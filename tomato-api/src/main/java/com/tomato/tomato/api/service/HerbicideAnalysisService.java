package com.tomato.tomato.api.service;

import com.tomato.tomato.api.controller.response.HerbicideAnalysisResponse;
import com.tomato.tomato.api.model.HerbicideAnalysisRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface HerbicideAnalysisService {

    ResponseEntity<HerbicideAnalysisResponse> analyzeHerbicide(
            MultipartFile image,
            String soilType,
            String growthStage,
            Double temperature,
            Double humidity,
            Double latitude,
            Double longitude,
            Boolean useNewModel
    ) throws IOException;

    // New methods for history management
    List<HerbicideAnalysisRecord> getAllAnalysisHistory();

    List<HerbicideAnalysisRecord> getAnalysisHistoryByUser(String userId);

    Page<HerbicideAnalysisRecord> getAnalysisHistoryPaginated(Pageable pageable);

    Page<HerbicideAnalysisRecord> getAnalysisHistoryByUserPaginated(String userId, Pageable pageable);

    List<HerbicideAnalysisRecord> getAnalysisHistoryByWeed(String weedName);

    List<HerbicideAnalysisRecord> getAnalysisHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<HerbicideAnalysisRecord> getRecentAnalysisHistory(int days);

    HerbicideAnalysisRecord getAnalysisRecordById(Long id);

    void deleteAnalysisRecord(Long id);

    long getTotalAnalysisCount();

    long getAnalysisCountByUser(String userId);
}