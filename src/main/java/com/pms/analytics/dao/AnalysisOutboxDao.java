package com.pms.analytics.dao;

import com.pms.analytics.dao.entity.AnalysisOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnalysisOutboxDao extends JpaRepository<AnalysisOutbox, UUID> {
    List<AnalysisOutbox> findByStatus(String status);
}
