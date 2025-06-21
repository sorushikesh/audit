package com.sorushi.invoice.management.audit.controller;

import static com.sorushi.invoice.management.audit.constants.APIEndpoints.*;
import static com.sorushi.invoice.management.audit.constants.Constants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.dto.AuditEventLoggedResponse;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import com.sorushi.invoice.management.audit.service.serviceImpl.AuditServiceImpl;
import org.javers.core.commit.CommitMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping(API_AUDIT_SERVICE)
public class AuditController {

  private final AuditServiceImpl auditService;

  public AuditController(AuditServiceImpl auditService) {
    this.auditService = auditService;
  }

  @PostMapping(LOG_DATA)
  public ResponseEntity<AuditEventLoggedResponse> logAuditEvent(@RequestBody AuditEvent auditEvent)
      throws JsonProcessingException, AuditServiceException {

    log.info("Processing audit event from {}", auditEvent.author());
    auditEvent = auditService.processAuditEvent(auditEvent);

    AuditEventLoggedResponse auditEventLoggedResponse =
        AuditEventLoggedResponse.builder()
            .result(RESPONSE_RESULT_SUCCESS)
            .message(RESPONSE_MESSAGE_SUCCESS)
            .author(auditEvent.author())
            .operation(auditEvent.operation())
            .build();

    return ResponseEntity.status(HttpStatus.OK).body(auditEventLoggedResponse);
  }

  @GetMapping(FETCH_AUDIT_DATA)
  public ResponseEntity<java.util.List<CommitMetadata>> fetchAuditData() {
    return ResponseEntity.ok(auditService.fetchAllAuditData());
  }

  @GetMapping(FETCH_AUDIT_DATA_BY_ENTITY)
  public ResponseEntity<java.util.List<CommitMetadata>> fetchAuditDataByEntity(
      @PathVariable String entityType, @PathVariable String entityId) {
    return ResponseEntity.ok(auditService.fetchAuditDataForEntity(entityType, entityId));
  }
}
