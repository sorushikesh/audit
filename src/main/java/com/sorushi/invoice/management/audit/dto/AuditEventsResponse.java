package com.sorushi.invoice.management.audit.dto;

import java.util.List;
import lombok.Builder;
import org.javers.core.commit.CommitMetadata;

@Builder
public record AuditEventsResponse(String result, Long count, List<CommitMetadata> records) {}
