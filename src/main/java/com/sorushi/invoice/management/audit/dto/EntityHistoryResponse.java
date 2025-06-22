package com.sorushi.invoice.management.audit.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record EntityHistoryResponse(String result, Long count, List<EntityHistoryRecord> records) {}
