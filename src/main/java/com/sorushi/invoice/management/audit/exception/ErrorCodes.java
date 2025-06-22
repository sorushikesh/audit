package com.sorushi.invoice.management.audit.exception;

public class ErrorCodes {

  public static final String DATE_TIME_PARSING_ERROR = "AUDIT_1001";
  public static final String PARSE_FIELD_LIST_ERROR = "AUDIT_1002";
  public static final String MISSING_DATABASE_NAME = "AUDIT_1003";
  public static final String JAVERS_INIT_FAILED = "AUDIT_1004";
  public static final String KAFKA_SEND_FAILED = "AUDIT_1005";
  public static final String PROCESS_AUDIT_EVENT_FAILED = "AUDIT_1006";
  public static final String COMMIT_METADATA_SAVE_FAILED = "AUDIT_1007";

  private ErrorCodes() {}
}
