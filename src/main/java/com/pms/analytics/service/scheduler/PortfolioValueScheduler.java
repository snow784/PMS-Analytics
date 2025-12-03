package com.pms.analytics.service.scheduler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pms.analytics.dao.PositionDao;
import com.pms.analytics.dao.PortfolioValueHistoryDao;
import com.pms.analytics.dao.entity.PositionEntity;
import com.pms.analytics.dao.entity.PortfolioValueHistoryEntity;
import com.pms.analytics.service.currentPrice.RedisPriceCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioValueScheduler {

    private final PositionDao positionDao;
    private final RedisPriceCache priceCache;
    private final PortfolioValueHistoryDao historyDao;

    // Run every day at 23:59
    @Scheduled(cron = "0 59 23 * * ?")
    public void calculatePortfolioValue() {

        List<PositionEntity> positions = positionDao.findAll();

        // group by portfolioId
        positions.stream()
            .map(PositionEntity::getId)
            .map(PositionEntity.PositionKey::getPortfolioId)
            .distinct()
            .forEach(portfolioId -> {
                BigDecimal portfolioValue = positions.stream()
                        .filter(p -> p.getId().getPortfolioId().equals(portfolioId))
                        .map(p -> {
                            String symbol = p.getId().getSymbol();
                            BigDecimal price = priceCache.getPrice(symbol);
                            if (price == null) price = BigDecimal.ZERO;
                            return price.multiply(BigDecimal.valueOf(p.getHoldings()));
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                PortfolioValueHistoryEntity history = new PortfolioValueHistoryEntity();
                history.setPortfolioId(portfolioId);
                history.setDate(LocalDate.now());
                history.setPortfolioValue(portfolioValue);
                historyDao.save(history);
            });
    }
}
