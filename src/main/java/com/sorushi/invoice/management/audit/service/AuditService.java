package com.sorushi.invoice.management.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.dto.AuditEventsQuery;
import com.sorushi.invoice.management.audit.dto.AuditEventsResponse;

public interface AuditService {

  AuditEvent processAuditEvent(AuditEvent auditEvent) throws JsonProcessingException;

  AuditEventsResponse fetchAuditData(AuditEventsQuery query);

  AuditEventsResponse fetchAuditDataForEntity(
      String entityType, String entityId, AuditEventsQuery query);
}
