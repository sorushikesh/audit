package com.sorushi.invoice.management.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import org.javers.core.commit.CommitMetadata;

public interface AuditService {

  AuditEvent processAuditEvent(AuditEvent auditEvent) throws JsonProcessingException;

  java.util.List<CommitMetadata> fetchAllAuditData();

  java.util.List<CommitMetadata> fetchAuditDataForEntity(String entityType, String entityId);
}
