package com.pms.analytics.publisher;

import com.pms.analytics.dto.RiskEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, RiskEventDto> kafkaTemplate;
    private static final String TOPIC = "portfolio-risk-metrics";

    public void publish(RiskEventDto event) {
        CompletableFuture<SendResult<String, RiskEventDto>> future = kafkaTemplate.send(TOPIC, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.println("[EventPublisher] Failed to publish portfolio: " 
                        + event.getPortfolioId() + ", reason: " + ex.getMessage());
            } else {
                System.out.println("[EventPublisher] Successfully published portfolio: " 
                        + event.getPortfolioId());
            }
        });
    }

    public void publishBulk(List<RiskEventDto> events) {
        if (events == null || events.isEmpty()) {
            System.out.println("[EventPublisher] No events to publish");
            return;
        }

        System.out.println("[EventPublisher] Publishing bulk events: " + events.size());
        for (RiskEventDto event : events) {
            publish(event);
        }
    }
}
