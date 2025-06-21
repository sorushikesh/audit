package com.sorushi.invoice.management.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sorushi.invoice.management.audit.dto.AuditEvent;

public interface AuditService {

  AuditEvent processAuditEvent(AuditEvent auditEvent) throws JsonProcessingException;
}
