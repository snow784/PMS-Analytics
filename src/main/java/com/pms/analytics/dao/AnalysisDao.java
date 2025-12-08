package com.pms.analytics.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pms.analytics.dao.entity.AnalysisEntity;
import com.pms.analytics.dao.entity.AnalysisEntity.AnalysisKey;

public interface AnalysisDao extends JpaRepository<AnalysisEntity, AnalysisKey>{
    List<AnalysisEntity> findByIdPortfolioId(UUID portfolioId);
}
