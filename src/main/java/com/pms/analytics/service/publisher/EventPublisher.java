package com.pms.analytics.service.publisher;

import com.pms.analytics.dto.RiskEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, RiskEventDto> kafkaTemplate;
    private static final String TOPIC = "portfolio-risk-metrics";

    /**
     * Publish a single event
     */
    public void publish(RiskEventDto event) {
        System.out.println("Publishing event: " + event);
        kafkaTemplate.send(TOPIC, event.getPortfolioId().toString(), event);
    }

    /**
     * Publish multiple events in bulk
     */
    public void publishBulk(List<RiskEventDto> events) {
        for (RiskEventDto event : events) {
            System.out.println("Publishing event: " + event);
            kafkaTemplate.send(TOPIC, event.getPortfolioId().toString(), event);
        }
    }
}
