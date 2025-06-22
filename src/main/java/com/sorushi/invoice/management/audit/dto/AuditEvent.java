package com.sorushi.invoice.management.audit.dto;

import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.Id;

public record AuditEvent(
    @Id String id,
    String entityType,
    String entityId,
    String changedDate,
    String author,
    String authorEmail,
    String operation,
    Map<String, Object> newVal,
    List<String> fieldList) {}
