package com.sorushi.invoice.management.audit.kafka.producer;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import com.sorushi.invoice.management.audit.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventProducer {

  private final KafkaTemplate<String, AuditEvent> kafkaTemplate;
  private final MessageSource messageSource;

  @Value("${spring.kafka.topics.audit-topic}")
  private String auditTopic;

  public void sendAuditEvent(AuditEvent auditEvent) {
    try {
      kafkaTemplate.send(auditTopic, auditEvent.id(), auditEvent);
      log.info("Sent audit event with ID: {} to topic: {}", auditEvent.id(), auditTopic);
    } catch (Exception ex) {
      log.error("Failed to send AuditEvent with ID: {}", auditEvent.id(), ex);
      throw new AuditServiceException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.KAFKA_SEND_FAILED,
          new Object[] {auditEvent.id()},
          messageSource);
    }
  }
}
