package com.sorushi.invoice.management.audit.dto;

import lombok.Builder;

@Builder
public record AuditEventLoggedResponse(String result, String message, String author, String operation) {}
