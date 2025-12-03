package com.pms.analytics.service.scheduler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pms.analytics.dao.PositionDao;
import com.pms.analytics.dto.RiskEventDto;
import com.pms.analytics.service.RiskMetricsService;
import com.pms.analytics.service.publisher.EventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskMetricsScheduler {

    private final PositionDao positionDao;
    private final RiskMetricsService riskMetricsService;
    private final EventPublisher eventPublisher;

    @Scheduled(fixedRate = 10000) // every 5 mins
    public void computeAndPublishBulk() {

        List<RiskEventDto> events = positionDao.findAll().stream()
                .map(p -> p.getId().getPortfolioId())
                .distinct()
                .map(riskMetricsService::computeRiskEvent)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (!events.isEmpty()) {
            System.out.println("Publishing bulk risk events: " + events.size());
            eventPublisher.publishBulk(events);
        }
    }
}
