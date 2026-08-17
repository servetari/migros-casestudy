package com.migros.casestudy.barcode;

import com.migros.casestudy.barcode.event.BarcodeGeneratedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092",
        "app.kafka.topics.barcode-events=barcode-events"
})
@ActiveProfiles("test")
class BarcodeKafkaConfigurationTest {

    @Autowired
    private KafkaTemplate<String, BarcodeGeneratedEvent> kafkaTemplate;

    @Test
    void kafkaTemplateShouldBeConfigured() {
        assertNotNull(kafkaTemplate);
    }
}