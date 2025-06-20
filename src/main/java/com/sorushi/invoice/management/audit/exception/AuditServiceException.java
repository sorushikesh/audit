package com.sorushi.invoice.management.audit.exception;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class AuditServiceException extends ErrorResponseException {

  public AuditServiceException(
      HttpStatus status,
      ProblemDetail problemDetail,
      Throwable cause,
      String errorCode,
      Object[] errorArguments) {
    super(status, problemDetail, cause, errorCode, errorArguments);
  }

  public AuditServiceException(
      HttpStatus status, String errorCode, Object[] errorArguments, MessageSource messageSource) {
    super(
        status,
        ExceptionUtil.buildProblemDetail(status, errorCode, errorArguments, messageSource),
        null,
        errorCode,
        errorArguments);
  }
}
