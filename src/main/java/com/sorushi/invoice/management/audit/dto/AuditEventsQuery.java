package com.sorushi.invoice.management.audit.dto;

public record AuditEventsQuery(Integer limit, Integer skip, String startDate, String endDate) {}
