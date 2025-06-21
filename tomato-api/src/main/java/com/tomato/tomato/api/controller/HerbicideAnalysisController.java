package com.tomato.tomato.api.controller;

import com.tomato.tomato.api.controller.response.HerbicideAnalysisResponse;
import com.tomato.tomato.api.model.HerbicideAnalysisRecord;
import com.tomato.tomato.api.service.HerbicideAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/herbicide-analysis")
@RequiredArgsConstructor
public class HerbicideAnalysisController {

    private final HerbicideAnalysisService herbicideAnalysisService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HerbicideAnalysisResponse> analyzeHerbicide(
            @RequestParam("image") MultipartFile image,
            @RequestParam("soilType") String soilType,
            @RequestParam("growthStage") String growthStage,
            @RequestParam("temperature") Double temperature,
            @RequestParam("humidity") Double humidity,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "useNewModel", defaultValue = "false") Boolean useNewModel) throws IOException {

        return herbicideAnalysisService.analyzeHerbicide(image, soilType, growthStage, temperature, humidity, latitude, longitude, useNewModel);
    }

    // Get all analysis history
    @GetMapping("/history")
    public ResponseEntity<List<HerbicideAnalysisRecord>> getAllHistory() {
        List<HerbicideAnalysisRecord> history = herbicideAnalysisService.getAllAnalysisHistory();
        return ResponseEntity.ok(history);
    }

    // Get analysis history with pagination
    @GetMapping("/history/paginated")
    public ResponseEntity<Page<HerbicideAnalysisRecord>> getHistoryPaginated(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HerbicideAnalysisRecord> history = herbicideAnalysisService.getAnalysisHistoryPaginated(pageable);
        return ResponseEntity.ok(history);
    }

    // Get analysis history by user
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<List<HerbicideAnalysisRecord>> getHistoryByUser(@PathVariable String userId) {
        List<HerbicideAnalysisRecord> history = herbicideAnalysisService.getAnalysisHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }

    // Get analysis history by user with pagination
    @GetMapping("/history/user/{userId}/paginated")
    public ResponseEntity<Page<HerbicideAnalysisRecord>> getHistoryByUserPaginated(
            @PathVariable String userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<HerbicideAnalysisRecord> history = herbicideAnalysisService.getAnalysisHistoryByUserPaginated(userId, pageable);
        return ResponseEntity.ok(history);
    }

    // Get specific analysis record by ID
    @GetMapping("/history/{id}")
    public ResponseEntity<HerbicideAnalysisRecord> getAnalysisById(@PathVariable Long id) {
        HerbicideAnalysisRecord record = herbicideAnalysisService.getAnalysisRecordById(id);
        return ResponseEntity.ok(record);
    }

    // Get analysis history by weed name
    @GetMapping("/history/weed/{weedName}")
    public ResponseEntity<List<HerbicideAnalysisRecord>> getHistoryByWeed(@PathVariable String weedName) {
        List<HerbicideAnalysisRecord> history = herbicideAnalysisService.getAnalysisHistoryByWeed(weedName);
        return ResponseEntity.ok(history);
    }

    // Get analysis history by date range
    @GetMapping("/history/date-range")
    public ResponseEntity<List<HerbicideAnalysisRecord>> getHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<HerbicideAnalysisRecord> history = herbicideAnalysisService.getAnalysisHistoryByDateRange(startDate, endDate);
        return ResponseEntity.ok(history);
    }

    // Get recent analysis history (last N days)
    @GetMapping("/history/recent")
    public ResponseEntity<List<HerbicideAnalysisRecord>> getRecentHistory(
            @RequestParam(value = "days", defaultValue = "7") int days) {

        List<HerbicideAnalysisRecord> history = herbicideAnalysisService.getRecentAnalysisHistory(days);
        return ResponseEntity.ok(history);
    }

    // Delete analysis record
    @DeleteMapping("/history/{id}")
    public ResponseEntity<Map<String, String>> deleteAnalysisRecord(@PathVariable Long id) {
        herbicideAnalysisService.deleteAnalysisRecord(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Analysis record deleted successfully");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    // Get analysis statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAnalysisStatistics(
            @RequestParam(value = "userId", required = false) String userId) {

        Map<String, Object> statistics = new HashMap<>();

        if (userId != null) {
            statistics.put("userAnalysisCount", herbicideAnalysisService.getAnalysisCountByUser(userId));
            statistics.put("userRecentHistory", herbicideAnalysisService.getAnalysisHistoryByUser(userId).stream().limit(5).toList());
        }

        statistics.put("totalAnalysisCount", herbicideAnalysisService.getTotalAnalysisCount());
        statistics.put("recentAnalyses", herbicideAnalysisService.getRecentAnalysisHistory(7));

        return ResponseEntity.ok(statistics);
    }

    // Search analysis history
    @GetMapping("/history/search")
    public ResponseEntity<List<HerbicideAnalysisRecord>> searchHistory(
            @RequestParam(value = "weedName", required = false) String weedName,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "herbicideName", required = false) String herbicideName,
            @RequestParam(value = "days", required = false) Integer days) {

        List<HerbicideAnalysisRecord> results;

        if (weedName != null) {
            results = herbicideAnalysisService.getAnalysisHistoryByWeed(weedName);
        } else if (userId != null) {
            results = herbicideAnalysisService.getAnalysisHistoryByUser(userId);
        } else if (days != null) {
            results = herbicideAnalysisService.getRecentAnalysisHistory(days);
        } else {
            results = herbicideAnalysisService.getAllAnalysisHistory();
        }

        return ResponseEntity.ok(results);
    }
}