package com.pms.analytics.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pms.analytics.dao.PortfolioValueHistoryDao;
import com.pms.analytics.dao.entity.PortfolioValueHistoryEntity;
import com.pms.analytics.dto.RiskEventDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskMetricsService {

    private final PortfolioValueHistoryDao historyDao;

    private static final int SCALE = 8;
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    /**
     * Compute risk metrics for a portfolio and return the RiskEventDto.
     */
    public Optional<RiskEventDto> computeRiskEvent(UUID portfolioId) {

        List<PortfolioValueHistoryEntity> last30Days = historyDao
                .findTop30ByPortfolioIdOrderByDateDesc(portfolioId);

        if (last30Days.size() < 2) return Optional.empty();

        BigDecimal previousValue = last30Days.get(last30Days.size() - 1).getPortfolioValue();
        BigDecimal totalReturn = last30Days.get(0).getPortfolioValue().subtract(previousValue);
        BigDecimal cumulativeReturn = totalReturn.divide(previousValue, SCALE, RoundingMode.HALF_UP);

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal sumNegativeSquared = BigDecimal.ZERO;
        int negativeCount = 0;

        for (int i = last30Days.size() - 1; i > 0; i--) {
            BigDecimal today = last30Days.get(i - 1).getPortfolioValue();
            BigDecimal yesterday = last30Days.get(i).getPortfolioValue();
            BigDecimal dailyReturn = today.subtract(yesterday).divide(yesterday, SCALE, RoundingMode.HALF_UP);

            sum = sum.add(dailyReturn);
            if (dailyReturn.compareTo(BigDecimal.ZERO) < 0) {
                sumNegativeSquared = sumNegativeSquared.add(dailyReturn.pow(2, MC));
                negativeCount++;
            }
        }

        BigDecimal avgDailyReturn = sum.divide(BigDecimal.valueOf(last30Days.size() - 1), SCALE, RoundingMode.HALF_UP);

        BigDecimal variance = BigDecimal.ZERO;
        for (int i = last30Days.size() - 1; i > 0; i--) {
            BigDecimal today = last30Days.get(i - 1).getPortfolioValue();
            BigDecimal yesterday = last30Days.get(i).getPortfolioValue();
            BigDecimal dailyReturn = today.subtract(yesterday).divide(yesterday, SCALE, RoundingMode.HALF_UP);
            variance = variance.add((dailyReturn.subtract(avgDailyReturn)).pow(2, MC));
        }
        BigDecimal stdDev = variance.divide(BigDecimal.valueOf(last30Days.size() - 2), MC).sqrt(MC);

        BigDecimal downsideDev = negativeCount > 0 ?
                sumNegativeSquared.divide(BigDecimal.valueOf(negativeCount), MC).sqrt(MC) :
                BigDecimal.ZERO;

        RiskEventDto event = new RiskEventDto(
                portfolioId,
                cumulativeReturn.floatValue(),
                stdDev.compareTo(BigDecimal.ZERO) > 0 ? avgDailyReturn.divide(stdDev, MC).floatValue() : 0f,
                downsideDev.compareTo(BigDecimal.ZERO) > 0 ? avgDailyReturn.divide(downsideDev, MC).floatValue() : 0f
        );

        return Optional.of(event);
    }
}
