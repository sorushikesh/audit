package com.sorushi.invoice.management.audit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.constants.Constants;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.dto.AuditEventLoggedResponse;
import com.sorushi.invoice.management.audit.dto.AuditEventsQuery;
import com.sorushi.invoice.management.audit.dto.AuditEventsResponse;
import com.sorushi.invoice.management.audit.dto.EntityHistoryResponse;
import com.sorushi.invoice.management.audit.kafka.producer.AuditEventProducer;
import com.sorushi.invoice.management.audit.service.serviceImpl.AuditServiceImpl;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuditControllerTest extends BaseContainerTest {

  private AuditServiceImpl service;
  private AuditEventProducer producer;
  private AuditController controller;

  @BeforeEach
  void setUp() {
    service = mock(AuditServiceImpl.class);
    producer = mock(AuditEventProducer.class);
    controller = new AuditController(service, producer);
  }

  @Test
  void logAuditEventReturnsSuccess() {
    AuditEvent event =
        new AuditEvent(
            "id",
            "type",
            "1",
            "now",
            "author",
            "a@example.com",
            "op",
            Map.of(),
            null);

    ResponseEntity<AuditEventLoggedResponse> resp = controller.logAuditEvent(event);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    AuditEventLoggedResponse body = resp.getBody();
    assertNotNull(body);
    assertEquals(Constants.RESPONSE_RESULT_SUCCESS, body.result());
    assertEquals("author", body.author());
    assertEquals("op", body.operation());
    verify(producer).sendAuditEvent(event);
  }

  @Test
  void fetchAuditDataInvokesService() {
    AuditEventsResponse response =
        AuditEventsResponse.builder()
            .result(Constants.RESPONSE_RESULT_SUCCESS)
            .count(0L)
            .records(Collections.emptyList())
            .build();
    when(service.fetchAuditData(any(AuditEventsQuery.class))).thenReturn(response);

    ResponseEntity<AuditEventsResponse> resp =
        controller.fetchAuditData(5, 1, "2024-01-01T00:00:00", "2024-01-02T00:00:00");

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals(response, resp.getBody());
    verify(service).fetchAuditData(any(AuditEventsQuery.class));
  }

  @Test
  void fetchAuditDataByEntityInvokesService() {
    AuditEventsResponse response =
        AuditEventsResponse.builder()
            .result(Constants.RESPONSE_RESULT_SUCCESS)
            .count(1L)
            .records(Collections.emptyList())
            .build();
    when(service.fetchAuditDataForEntity(eq("type"), eq("1"), any(AuditEventsQuery.class)))
        .thenReturn(response);

    ResponseEntity<AuditEventsResponse> resp =
        controller.fetchAuditDataByEntity("type", "1", null, null, null, null);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals(response, resp.getBody());
    verify(service).fetchAuditDataForEntity(eq("type"), eq("1"), any(AuditEventsQuery.class));
  }

  @Test
  void fetchAuditDataByUserInvokesService() {
    AuditEventsResponse response =
        AuditEventsResponse.builder()
            .result(Constants.RESPONSE_RESULT_SUCCESS)
            .count(1L)
            .records(Collections.emptyList())
            .build();
    when(service.fetchAuditDataForUser(eq("user"), any(AuditEventsQuery.class)))
        .thenReturn(response);

    ResponseEntity<AuditEventsResponse> resp =
        controller.fetchAuditDataByUser("user", null, null, null, null);

    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals(response, resp.getBody());
    verify(service).fetchAuditDataForUser(eq("user"), any(AuditEventsQuery.class));
  }

  @Test
  void fetchEntityHistoryInvokesService() {
    EntityHistoryResponse resp =
        EntityHistoryResponse.builder()
            .result(Constants.RESPONSE_RESULT_SUCCESS)
            .count(0L)
            .records(Collections.emptyList())
            .build();
    when(service.fetchEntityHistory("type", "1")).thenReturn(resp);

    ResponseEntity<EntityHistoryResponse> response = controller.fetchEntityHistory("type", "1");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(resp, response.getBody());
    verify(service).fetchEntityHistory("type", "1");
  }
}
