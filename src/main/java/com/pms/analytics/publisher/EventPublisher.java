package com.pms.analytics.publisher;

import com.pms.analytics.dao.AnalysisOutboxDao;
import com.pms.analytics.dao.entity.AnalysisOutbox;
import com.pms.analytics.dto.RiskEventDto;
import com.pms.analytics.dto.RiskEventOuterClass;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, RiskEventOuterClass.RiskEvent> kafkaTemplate;
    private final AnalysisOutboxDao analysisOutboxDao;

    private static final String TOPIC = "portfolio-risk-metrics";

    public void publishPendingEvents() {

        List<AnalysisOutbox> pendingEvents = analysisOutboxDao.findByStatus("PENDING");

        for (AnalysisOutbox outbox : pendingEvents) {
            try {

                RiskEventOuterClass.RiskEvent event = RiskEventOuterClass.RiskEvent.parseFrom(outbox.getPayload());

                kafkaTemplate.send(TOPIC, outbox.getPortfolioId().toString(), event)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                System.out.println("Publisher Successfully sent: " + outbox.getPortfolioId()
                                        + " | Kafka Offset: " + result.getRecordMetadata().offset());

                                outbox.setStatus("SENT");
                                analysisOutboxDao.save(outbox);
                            } else {
                                System.err.println("Publisher Failed to send: " + outbox.getPortfolioId()
                                        + " | Reason: " + ex.getMessage());

                            }
                        });

            } catch (Exception e) {
                System.err.println("Publisher Failed to parse proto for outbox: " + outbox.getOutboxId()
                        + " | Reason: " + e.getMessage());

                outbox.setStatus("FAILED");
                analysisOutboxDao.save(outbox);
            }
        }
    }
}
