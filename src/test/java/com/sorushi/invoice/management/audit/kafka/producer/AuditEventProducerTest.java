package com.sorushi.invoice.management.audit.kafka.producer;

import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import com.sorushi.invoice.management.audit.BaseContainerTest;

class AuditEventProducerTest extends BaseContainerTest {

  @Test
  void sendAuditEventSuccess() {
    KafkaTemplate<String, AuditEvent> template = mock(KafkaTemplate.class);
    AuditEventProducer producer = new AuditEventProducer(template);
    AuditEvent event = new AuditEvent("id", "t", "1", null, null, null, Map.of(), null);
    producer.sendAuditEvent(event);
    verify(template).send(anyString(), eq("id"), eq(event));
  }

  @Test
  void sendAuditEventFailure() {
    KafkaTemplate<String, AuditEvent> template = mock(KafkaTemplate.class);
    doThrow(new RuntimeException("fail")).when(template).send(anyString(), anyString(), any());
    AuditEventProducer producer = new AuditEventProducer(template);
    AuditEvent event = new AuditEvent("id", "t", "1", null, null, null, Map.of(), null);
    producer.sendAuditEvent(event); // should swallow exception
  }
}
