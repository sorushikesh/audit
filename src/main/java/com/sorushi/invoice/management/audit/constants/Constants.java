package com.sorushi.invoice.management.audit.constants;

public class Constants {

  public static final String ERRORS = "errors";

  public static final String SLASH = "/";
  public static final String DOT = ".";

  public static final String DEFAULT_USER = "System user";

  public static final String TYPE = "type";
  public static final String TYPE_ID = "typeId";
  public static final String OPERATION = "operation";
  public static final String USER_ID = "userId";
  public static final String CHANGED_DATE = "changedDate";

  public static final String OPERATION_READ = "Read";
  public static final String OPERATION_DELETE = "Delete";

  public static final String FIELD_PROPERTIES = "properties";
  public static final String FIELD_UNDERSCORE_ID = "_id";
  public static final String FIELD_ID = "id";
  public static final String FIELD_MAJOR_ID = "majorId";
  public static final String FIELD_COMMIT_DATE = "commitDate";
  public static final String FIELD_COMMIT_META_DATA = "commitMetadata";
  public static final String FIELD_TTL_DATE = "TTL_DATE";
  public static final String COLLECTION_COMMIT_METADATA_COLLECTION = "commit_metadata";
  public static final String COLLECTION_JV_SNAPSHOT = "jv_snapshots";

  public static final String RESPONSE_RESULT_SUCCESS = "Success";
  public static final String RESPONSE_MESSAGE_SUCCESS = "Audit event logged successfully";

  private Constants() {}
}
