package com.sorushi.invoice.management.audit.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class ExceptionUtilTest {

  @Container
  static final GenericContainer<?> CONTAINER =
      new GenericContainer<>(DockerImageName.parse("alpine:3.19")).withCommand("sleep", "1");

  private StaticMessageSource messageSource;

  @BeforeEach
  void setUp() {
    messageSource = new StaticMessageSource();
    messageSource.addMessage("ERR", Locale.ENGLISH, "message");
  }

  @Test
  void buildProblemDetailNormal() {
    ProblemDetail detail =
        ExceptionUtil.buildProblemDetail(
            HttpStatus.BAD_REQUEST, "ERR", new Object[] {"a"}, messageSource);
    assertEquals("ERR : message", detail.getDetail());
  }

  @Test
  void buildProblemDetailWithNulls() {
    ProblemDetail missingSource =
        ExceptionUtil.buildProblemDetail(HttpStatus.BAD_REQUEST, "ERR", null, null);
    assertEquals("Message source is null", missingSource.getDetail());

    ProblemDetail missingCode =
        ExceptionUtil.buildProblemDetail(HttpStatus.BAD_REQUEST, null, null, messageSource);
    assertEquals("ErrorCode is null", missingCode.getDetail());

    ProblemDetail nullStatus = ExceptionUtil.buildProblemDetail(null, "ERR", null, messageSource);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, nullStatus.getStatus());
  }
}
