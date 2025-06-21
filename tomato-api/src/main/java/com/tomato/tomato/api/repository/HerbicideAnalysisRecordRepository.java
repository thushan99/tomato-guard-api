package com.tomato.tomato.api.repository;

import com.tomato.tomato.api.model.HerbicideAnalysisRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HerbicideAnalysisRecordRepository extends JpaRepository<HerbicideAnalysisRecord, Long> {

    // Find all records ordered by creation date (newest first)
    List<HerbicideAnalysisRecord> findAllByOrderByCreatedAtDesc();

    // Find records by user ID
    List<HerbicideAnalysisRecord> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find records by user ID with pagination
    Page<HerbicideAnalysisRecord> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Find records by weed name
    List<HerbicideAnalysisRecord> findByWeedNameIgnoreCaseOrderByCreatedAtDesc(String weedName);

    // Find records by analysis status
    List<HerbicideAnalysisRecord> findByAnalysisStatusOrderByCreatedAtDesc(String analysisStatus);

    // Find records within date range
    @Query("SELECT h FROM HerbicideAnalysisRecord h WHERE h.createdAt BETWEEN :startDate AND :endDate ORDER BY h.createdAt DESC")
    List<HerbicideAnalysisRecord> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    // Find recent records (last N days)
    @Query("SELECT h FROM HerbicideAnalysisRecord h WHERE h.createdAt >= :date ORDER BY h.createdAt DESC")
    List<HerbicideAnalysisRecord> findRecentRecords(@Param("date") LocalDateTime date);

    // Count records by user
    long countByUserId(String userId);

    // Count successful analyses
    long countByAnalysisStatus(String analysisStatus);

    // Find records by herbicide name
    List<HerbicideAnalysisRecord> findByPredictedHerbicideNameIgnoreCaseOrderByCreatedAtDesc(String herbicideName);

    // Find records with pagination
    Page<HerbicideAnalysisRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);
}