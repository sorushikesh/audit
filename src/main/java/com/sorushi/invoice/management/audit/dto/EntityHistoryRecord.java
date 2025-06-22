package com.sorushi.invoice.management.audit.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Date;
import lombok.Builder;

@Builder
public record EntityHistoryRecord(
    String author,
    String operation,
    String changedDate,
    Date commitDate,
    JsonNode oldValue,
    JsonNode newValue) {}
