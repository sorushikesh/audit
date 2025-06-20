package com.sorushi.invoice.management.audit.service;

import com.sorushi.invoice.management.audit.dto.AuditEvent;

public interface AuditService {

  void processAuditEvent(AuditEvent auditEvent);
}
