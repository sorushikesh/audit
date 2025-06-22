package com.sorushi.invoice.management.audit.kafka.listener;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.exception.ErrorCodes;
import com.sorushi.invoice.management.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditKafkaListener {

  private final AuditService auditService;

  @KafkaListener(topics = "audit-log", groupId = "audit-service-group")
  public void listen(AuditEvent auditEvent) {
    try {
      auditService.processAuditEvent(auditEvent);
      log.info("Processed audit event with ID: {}", auditEvent.entityId());
    } catch (Exception e) {
      log.error(
          "{}: Failed to process audit event with ID: {}",
          ErrorCodes.PROCESS_AUDIT_EVENT_FAILED,
          auditEvent.entityId(),
          e);
    }
  }
}
