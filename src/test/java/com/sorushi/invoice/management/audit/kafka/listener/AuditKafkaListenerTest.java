package com.sorushi.invoice.management.audit.kafka.listener;

import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.service.AuditService;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditKafkaListenerTest extends BaseContainerTest {

  @Test
  void listenSuccess() throws Exception {
    AuditService service = mock(AuditService.class);
    AuditKafkaListener listener = new AuditKafkaListener(service);
    AuditEvent event = new AuditEvent("id", "t", "1", null, null, null, null, Map.of(), null);
    listener.listen(event);
    verify(service).processAuditEvent(event);
  }

  @Test
  void listenFailure() throws Exception {
    AuditService service = mock(AuditService.class);
    doThrow(new RuntimeException("fail")).when(service).processAuditEvent(any());
    AuditKafkaListener listener = new AuditKafkaListener(service);
    AuditEvent event = new AuditEvent("id", "t", "1", null, null, null, null, Map.of(), null);
    listener.listen(event); // should handle exception internally
  }
}
