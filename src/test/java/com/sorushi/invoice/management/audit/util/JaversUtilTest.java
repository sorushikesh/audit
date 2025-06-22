package com.sorushi.invoice.management.audit.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.model.AuditEventJavers;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JaversUtilTest extends BaseContainerTest {

  @Test
  void propertiesMapOperations() {
    JaversUtil.clearPropertiesMap();
    JaversUtil.addJaversCommitProperties("k", "v");
    assertEquals(Map.of("k", "v"), JaversUtil.getJaversCommitPropertiesMap());
  }

  @Test
  void setPayloadInModelWithFieldList() throws Exception {
    JaversUtil util = new JaversUtil(mock(MongoClient.class));
    ObjectMapper mapper = new ObjectMapper();
    JsonNode filtered = mapper.readTree("{\"name\":\"Joe\"}");
    ObjectNode original = mapper.createObjectNode();
    AuditEventJavers javers = new AuditEventJavers();
    AuditEvent event =
        new AuditEvent("id", "type", "1", null, null, null, Map.of("name", "Joe"), List.of("name"));
    util.setPayloadInModel(event, javers, filtered, original);
    assertEquals("Joe", javers.getJsonNode().get("name").asText());
  }

  @Test
  void setPayloadInModelWithoutFieldList() throws Exception {
    JaversUtil util = new JaversUtil(mock(MongoClient.class));
    ObjectMapper mapper = new ObjectMapper();
    JsonNode filtered = mapper.readTree("{\"name\":\"Joe\"}");
    ObjectNode original = mapper.createObjectNode();
    AuditEventJavers javers = new AuditEventJavers();
    AuditEvent event =
        new AuditEvent("id", "type", "1", null, null, null, Map.of("name", "Joe"), null);
    util.setPayloadInModel(event, javers, filtered, original);
    assertEquals(filtered, javers.getJsonNode());
  }
}
