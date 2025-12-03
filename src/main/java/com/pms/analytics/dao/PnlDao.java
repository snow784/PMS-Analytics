package com.pms.analytics.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pms.analytics.dao.entity.PnlEntity;

public interface PnlDao extends JpaRepository<PnlEntity, UUID> {

    // Fetch all active symbols across portfolios
    @Query("SELECT DISTINCT p.symbol FROM PnlEntity p WHERE p.remainingQuantity > 0")
    List<String> findAllActiveSymbols();

    // Fetch distinct portfolio IDs with open positions
    @Query("SELECT DISTINCT p.portfolioId FROM PnlEntity p WHERE p.remainingQuantity > 0")
    List<UUID> findDistinctPortfolioIdsWithOpenPositions();

    // Fetch open transactions for a portfolio
    List<PnlEntity> findByPortfolioIdAndRemainingQuantityGreaterThan(UUID portfolioId, long qty);
}
