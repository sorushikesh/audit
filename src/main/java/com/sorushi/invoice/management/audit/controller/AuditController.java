package com.sorushi.invoice.management.audit.controller;

import static com.sorushi.invoice.management.audit.constants.APIEndpoints.*;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.service.serviceImpl.AuditServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(API_AUDIT_SERVICE)
public class AuditController {

  private final AuditServiceImpl auditService;

  public AuditController(AuditServiceImpl auditService) {
    this.auditService = auditService;
  }

  @PostMapping(LOG_DATA)
  public void logAudit(@RequestBody AuditEvent auditEvent) {

    log.info("Processing audit event from {}", auditEvent.author());
    auditService.processAuditEvent(auditEvent);
  }
}
