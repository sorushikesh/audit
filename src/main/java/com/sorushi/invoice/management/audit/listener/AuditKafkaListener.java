package com.sorushi.invoice.management.audit.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditKafkaListener {

  private final AuditService auditService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @KafkaListener(topics = "audit-log")
  public void listen(ConsumerRecord<String, String> record) {
    String message = record.value();
    try {
      AuditEvent auditEvent = objectMapper.readValue(message, AuditEvent.class);
      auditService.processAuditEvent(auditEvent);
    } catch (Exception e) {
      log.error("Failed to process audit event: {}", message, e);
    }
  }
}
