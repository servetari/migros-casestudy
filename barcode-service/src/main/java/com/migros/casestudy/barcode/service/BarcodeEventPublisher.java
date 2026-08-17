package com.migros.casestudy.barcode.service;

import com.migros.casestudy.barcode.entity.Barcode;
import com.migros.casestudy.barcode.event.BarcodeGeneratedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BarcodeEventPublisher {
    private final KafkaTemplate<String, BarcodeGeneratedEvent> kafkaTemplate;
    private final String topic;

    public BarcodeEventPublisher(
            KafkaTemplate<String, BarcodeGeneratedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.barcode-events}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(Barcode barcode) {
        BarcodeGeneratedEvent event = new BarcodeGeneratedEvent(
                barcode.getId(), barcode.getCode(), barcode.getType(), barcode.getProductId()
        );
        kafkaTemplate.send(topic, String.valueOf(barcode.getProductId()), event);
    }
}