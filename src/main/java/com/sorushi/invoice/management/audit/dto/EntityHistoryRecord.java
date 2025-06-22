package com.sorushi.invoice.management.audit.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record EntityHistoryRecord(
    String author,
    String operation,
    String changedDate,
    LocalDateTime commitDate,
    JsonNode oldValue,
    JsonNode newValue) {}
