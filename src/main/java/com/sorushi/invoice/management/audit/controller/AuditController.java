package com.sorushi.invoice.management.audit.controller;

import static com.sorushi.invoice.management.audit.constants.APIEndpoints.*;
import static com.sorushi.invoice.management.audit.constants.Constants.*;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.dto.AuditEventLoggedResponse;
import com.sorushi.invoice.management.audit.dto.AuditEventsQuery;
import com.sorushi.invoice.management.audit.dto.AuditEventsResponse;
import com.sorushi.invoice.management.audit.dto.EntityHistoryResponse;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import com.sorushi.invoice.management.audit.kafka.producer.AuditEventProducer;
import com.sorushi.invoice.management.audit.service.serviceImpl.AuditServiceImpl;
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
  private final AuditEventProducer auditEventProducer;

  public AuditController(AuditServiceImpl auditService, AuditEventProducer auditEventProducer) {
    this.auditService = auditService;
    this.auditEventProducer = auditEventProducer;
  }

  @PostMapping(LOG_DATA)
  public ResponseEntity<AuditEventLoggedResponse> logAuditEvent(@RequestBody AuditEvent auditEvent)
      throws AuditServiceException {
    log.info(
        "Received request to log audit event: author={}, operation={}, entityType={}, entityId={}",
        auditEvent.author(),
        auditEvent.operation(),
        auditEvent.entityType(),
        auditEvent.entityId());

    auditEventProducer.sendAuditEvent(auditEvent);
    AuditEventLoggedResponse response =
        AuditEventLoggedResponse.builder()
            .result(RESPONSE_RESULT_SUCCESS)
            .message(RESPONSE_MESSAGE_SUCCESS)
            .author(auditEvent.author())
            .operation(auditEvent.operation())
            .build();

    log.info(
        "Audit event logged successfully: author={}, operation={}",
        response.author(),
        response.operation());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping(FETCH_AUDIT_DATA)
  public ResponseEntity<AuditEventsResponse> fetchAuditData(
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) Integer skip,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    log.info(
        "Received request to fetch audit data with params - limit: {}, skip: {}, startDate: {}, endDate: {}",
        limit,
        skip,
        startDate,
        endDate);

    AuditEventsQuery query = new AuditEventsQuery(limit, skip, startDate, endDate);
    AuditEventsResponse response = auditService.fetchAuditData(query);

    log.info("Fetched {} audit events.", response.count());
    return ResponseEntity.ok(response);
  }

  @GetMapping(FETCH_AUDIT_DATA_BY_ENTITY)
  public ResponseEntity<AuditEventsResponse> fetchAuditDataByEntity(
      @PathVariable String entityType,
      @PathVariable String entityId,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) Integer skip,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    log.info(
        "Received request to fetch audit data by entity - entityType: {}, entityId: {}, limit: {}, skip: {}, startDate: {}, endDate: {}",
        entityType,
        entityId,
        limit,
        skip,
        startDate,
        endDate);

    AuditEventsQuery query = new AuditEventsQuery(limit, skip, startDate, endDate);
    AuditEventsResponse response =
        auditService.fetchAuditDataForEntity(entityType, entityId, query);

    log.info("Fetched {} audit events for entity [{}:{}].", response.count(), entityType, entityId);
    return ResponseEntity.ok(response);
  }

  @GetMapping(FETCH_AUDIT_DATA_BY_USER)
  public ResponseEntity<AuditEventsResponse> fetchAuditDataByUser(
      @PathVariable String userId,
      @RequestParam(required = false) Integer limit,
      @RequestParam(required = false) Integer skip,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {

    log.info(
        "Received request to fetch audit data by user - userId: {}, limit: {}, skip: {}, startDate: {}, endDate: {}",
        userId,
        limit,
        skip,
        startDate,
        endDate);

    AuditEventsQuery query = new AuditEventsQuery(limit, skip, startDate, endDate);
    AuditEventsResponse response = auditService.fetchAuditDataForUser(userId, query);

    log.info("Fetched {} audit events for user {}.", response.count(), userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping(FETCH_ENTITY_HISTORY)
  public ResponseEntity<EntityHistoryResponse> fetchEntityHistory(
      @PathVariable String entityType, @PathVariable String entityId) {
    log.info(
        "Received request to fetch history for entityType: {}, entityId: {}", entityType, entityId);
    EntityHistoryResponse response = auditService.fetchEntityHistory(entityType, entityId);
    log.info(
        "Fetched {} history records for entity [{}:{}]", response.count(), entityType, entityId);
    return ResponseEntity.ok(response);
  }
}
