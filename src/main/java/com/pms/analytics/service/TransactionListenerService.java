package com.pms.analytics.service;

import com.pms.analytics.dto.TransactionDto;
import com.pms.analytics.dto.TransactionOuterClass.Transaction;
import com.pms.analytics.mapper.TransactionMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionListenerService {

    @KafkaListener(
            topics = "transactions-topic",
            groupId = "demo-group",
            containerFactory = "protobufKafkaListenerContainerFactory"
    )
    public void listen(Transaction message) {
        TransactionDto dto = TransactionMapper.fromProto(message);
        System.out.println("Received Transaction DTO: " + dto);
    }
}
