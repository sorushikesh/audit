package com.sorushi.invoice.management.audit.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import com.sorushi.invoice.management.audit.BaseContainerTest;

class AuditServiceExceptionHandlerTest extends BaseContainerTest {

  private StaticMessageSource messageSource;
  private AuditServiceExceptionHandler handler;

  @BeforeEach
  void setUp() {
    messageSource = new StaticMessageSource();
    messageSource.addMessage("ERR", Locale.ENGLISH, "msg");
    handler = new AuditServiceExceptionHandler(messageSource);
  }

  @Test
  void handleAuditServiceException() {
    AuditServiceException ex =
        new AuditServiceException(HttpStatus.BAD_REQUEST, "ERR", null, messageSource);
    ResponseEntity<ProblemDetail> resp = handler.handleAuditServiceException(ex);
    assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    ProblemDetail pd = resp.getBody();
    assertNotNull(pd);
    assertEquals("ERR : msg", pd.getDetail());
    assertTrue(pd.getProperties().containsKey("errors"));
  }
}
