package com.sorushi.invoice.management.audit.kafka.producer;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class AuditEventProducerTest extends BaseContainerTest {

  @Test
  void sendAuditEventSuccess() {
    KafkaTemplate<String, AuditEvent> template = mock(KafkaTemplate.class);
    MessageSource messageSource = mock(MessageSource.class);
    AuditEventProducer producer = new AuditEventProducer(template, messageSource);
    ReflectionTestUtils.setField(producer, "auditTopic", "audit-log");
    AuditEvent event = new AuditEvent("id", "t", "1", null, null, null, Map.of(), null);
    producer.sendAuditEvent(event);
    verify(template).send(anyString(), eq("id"), eq(event));
  }

  @Test
  void sendAuditEventFailure() {
    KafkaTemplate<String, AuditEvent> template = mock(KafkaTemplate.class);
    MessageSource messageSource = mock(MessageSource.class);
    doThrow(new RuntimeException("fail")).when(template).send(anyString(), anyString(), any());
    AuditEventProducer producer = new AuditEventProducer(template, messageSource);
    ReflectionTestUtils.setField(producer, "auditTopic", "audit-log");
    AuditEvent event = new AuditEvent("id", "t", "1", null, null, null, Map.of(), null);
    assertThrows(AuditServiceException.class, () -> producer.sendAuditEvent(event));
  }
}
