package com.pms.analytics.scheduler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pms.analytics.dao.AnalysisDao;
import com.pms.analytics.dto.RiskEventDto;
import com.pms.analytics.publisher.EventPublisher;
import com.pms.analytics.service.RiskMetricsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskMetricsScheduler {

    private final AnalysisDao analysisDao;
    private final RiskMetricsService riskMetricsService;
    private final EventPublisher eventPublisher;

//    @Scheduled(fixedRate = 10000)
//    public void computeAndPublishBulk() {
//
//        List<UUID> portfolioIds = analysisDao.findAll().stream()
//                .map(a -> a.getId().getPortfolioId())
//                .distinct()
//                .collect(Collectors.toList());
//
//        if (portfolioIds.isEmpty()) return;
//
//        List<RiskEventDto> events = portfolioIds.stream()
//                .map(riskMetricsService::computeRiskEvent)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toList());
//
//        if (events.isEmpty()) return;
//
//        System.out.println("Publishing risk events: " + events.size());
//        eventPublisher.publishBulk(events);
//    }

    // Runs every 10 seconds (adjust as needed)
    @Scheduled(fixedRate = 10000)
    public void publishPendingRiskEvents() {
        System.out.println("[Scheduler] Checking for pending risk events to publish...");
        eventPublisher.publishPendingEvents();
    }

}
