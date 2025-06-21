package com.sorushi.invoice.management.audit.constants;

public class APIEndpoints {

  public static final String API_AUDIT_SERVICE = "/api/auditService";
  public static final String LOG_DATA = "/log";
  public static final String FETCH_AUDIT_DATA = "/audit";
  public static final String FETCH_AUDIT_DATA_BY_ENTITY = "/audit/{entityType}/{entityId}";

  private APIEndpoints() {}
}
