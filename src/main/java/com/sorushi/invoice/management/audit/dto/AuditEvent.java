package com.sorushi.invoice.management.audit.dto;

import java.util.List;
import java.util.Map;

public record AuditEvent(
    String id,
    String entityType,
    String entityId,
    long tenantId,
    String changedDate,
    String uniqueIdentifier,
    String author,
    String operation,
    Map<String, Object> newVal,
    List<String> fieldList) {}
