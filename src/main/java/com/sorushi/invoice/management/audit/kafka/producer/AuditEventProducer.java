package com.sorushi.invoice.management.audit.kafka.producer;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventProducer {

  private final KafkaTemplate<String, AuditEvent> kafkaTemplate;

  @Value("${spring.kafka.topics.audit-topic}")
  private String auditTopic;

  public void sendAuditEvent(AuditEvent auditEvent) {
    try {
      kafkaTemplate.send(auditTopic, auditEvent.id(), auditEvent);
      log.info("Sent audit event with ID: {} to topic: {}", auditEvent.id(), auditTopic);
    } catch (Exception ex) {
      log.error("Error while sending AuditEvent with ID: {}", auditEvent.id(), ex);
    }
  }
}
