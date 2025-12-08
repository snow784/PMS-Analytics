package com.pms.analytics.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pms.analytics.dao.entity.PositionEntity;
import com.pms.analytics.dao.entity.PositionEntity.PositionKey;

public interface PositionDao extends JpaRepository<PositionEntity, PositionKey> {
    
    // Or using UUID if your PortfolioId is UUID
    List<PositionEntity> findByIdPortfolioId(UUID portfolioId);
}
