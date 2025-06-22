package com.sorushi.invoice.management.audit.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class AuditHelperUtilTest {

  @Container
  static final GenericContainer<?> CONTAINER =
      new GenericContainer<>(DockerImageName.parse("alpine:3.19")).withCommand("sleep", "1");

  private AuditHelperUtil util;
  private StaticMessageSource messageSource;

  @BeforeEach
  void setUp() {
    messageSource = new StaticMessageSource();
    messageSource.addMessage("AUDIT_1001", Locale.ENGLISH, "bad date");
    messageSource.addMessage("AUDIT_1002", Locale.ENGLISH, "bad field");
    util = new AuditHelperUtil(messageSource);
  }

  @Test
  void validateDateSuccess() throws Exception {
    util.validateDate("2024-01-01T10:00:00", List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
  }

  @Test
  void validateDateFailure() {
    assertThrows(
        AuditServiceException.class,
        () -> util.validateDate("bad", List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
  }

  @Test
  void filterRequestFields() throws Exception {
    AuditEvent event =
        new AuditEvent(
            "id", "t", "1", "date", "author", "op", Map.of("name", "Joe"), List.of("name"));
    List<String> list = new java.util.ArrayList<>();
    util.filterRequestFields(event, list);
    assertEquals(1, list.size());
    assertEquals("/name", list.get(0));
  }

  @Test
  void filterRequestFieldsMissing() {
    AuditEvent event =
        new AuditEvent("id", "t", "1", "date", "author", "op", Map.of(), List.of("missing"));
    assertThrows(
        AuditServiceException.class,
        () -> util.filterRequestFields(event, new java.util.ArrayList<>()));
  }

  @Test
  void processFieldListIfPresentInRequest() throws Exception {
    AuditEvent event =
        new AuditEvent(
            "id", "t", "1", "date", "author", "op", Map.of("name", "Joe"), List.of("name"));
    List<String> list = new java.util.ArrayList<>();
    util.filterRequestFields(event, list);
    util.processFieldListIfPresentInRequest(event, list);
  }

  @Test
  void addCommitPropertiesMap() {
    AuditEvent event =
        new AuditEvent("id", "Customer", "123", "now", "auth", "create", Map.of(), null);
    Map<String, String> map = util.addCommitPropertiesMap(event);
    assertEquals("Customer", map.get("type"));
    assertEquals("123", map.get("typeId"));
    assertEquals("create", map.get("operation"));
    assertEquals("auth", map.get("userId"));
    assertEquals("now", map.get("changedDate"));
  }
}
