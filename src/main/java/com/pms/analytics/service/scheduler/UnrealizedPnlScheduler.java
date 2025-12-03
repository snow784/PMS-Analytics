package com.pms.analytics.service.scheduler;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pms.analytics.dao.PnlDao;
import com.pms.analytics.dao.entity.PnlEntity;
import com.pms.analytics.dto.UnrealizedPnlDto;
import com.pms.analytics.service.currentPrice.ExternalPriceClient;
import com.pms.analytics.service.currentPrice.RedisPriceCache;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UnrealizedPnlScheduler {

    private final PnlDao pnlDao;
    private final RedisPriceCache priceCache;
    private final ExternalPriceClient externalPriceClient;
    private final SimpMessagingTemplate messagingTemplate;

    // fallback local cache for last known prices
    private final Map<String, BigDecimal> lastKnownPrices = new ConcurrentHashMap<>();

    /**
     * Runs every 2 seconds
     */
    @Scheduled(fixedRateString = "10000")
    public void computeAndBroadcast() {
        try {
            // fetch all portfolios with open positions
            List<UUID> portfolioIds = pnlDao.findDistinctPortfolioIdsWithOpenPositions();

            for (UUID portfolioId : portfolioIds) {

                // fetch all open transactions for this portfolio
                List<PnlEntity> openTxns = pnlDao.findByPortfolioIdAndRemainingQuantityGreaterThan(portfolioId, 0);
                if (openTxns.isEmpty()) continue;

                Map<String, BigDecimal> symbolUnrealized = new HashMap<>();
                BigDecimal totalUnrealized = BigDecimal.ZERO;

                for (PnlEntity txn : openTxns) {
                    try {
                        String symbol = txn.getSymbol();
                        long remainingQty = txn.getRemainingQuantity();
                        BigDecimal buyPrice = txn.getBuyPrice();

                        if (remainingQty <= 0 || buyPrice == null) continue;

                        // get current price (Redis → lastKnown → external)
                        BigDecimal currentPrice = priceCache.getPrice(symbol);
                        if (currentPrice == null) {
                            currentPrice = lastKnownPrices.get(symbol);
                        }
                        if (currentPrice == null) {
                            try {
                                Mono<BigDecimal> mono = externalPriceClient.fetchPriceAsync(symbol);
                                currentPrice = mono.block(Duration.ofSeconds(3));
                            } catch (Exception e) {
                                currentPrice = null;
                            }
                        }
                        if (currentPrice == null) continue;

                        lastKnownPrices.put(symbol, currentPrice);

                        // unrealized = (currentPrice - buyPrice) * remainingQuantity
                        BigDecimal unrealized = currentPrice.subtract(buyPrice)
                                .multiply(BigDecimal.valueOf(remainingQty));

                        // sum per symbol
                        symbolUnrealized.put(symbol,
                                symbolUnrealized.getOrDefault(symbol, BigDecimal.ZERO).add(unrealized));

                        // sum total
                        totalUnrealized = totalUnrealized.add(unrealized);

                    } catch (Exception e) {
                        System.err.println("Error computing unrealized PnL for transaction: " + e.getMessage());
                    }
                }

                // prepare payload
                UnrealizedPnlDto payload = new UnrealizedPnlDto(symbolUnrealized, totalUnrealized, portfolioId.toString());

                // broadcast to frontend
                try {
                    messagingTemplate.convertAndSend("/topic/unrealized-pnl", payload);
                } catch (Exception e) {
                    System.err.println("Failed to send unrealized PnL over WebSocket: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Unrealized PnL scheduler error: " + e.getMessage());
        }
    }
}
