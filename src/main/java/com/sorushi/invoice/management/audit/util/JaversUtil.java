package com.sorushi.invoice.management.audit.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import com.sorushi.invoice.management.audit.exception.ErrorCodes;
import com.sorushi.invoice.management.audit.model.AuditEventJavers;
import jakarta.annotation.PostConstruct;
import java.util.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.repository.mongo.MongoRepository;
import org.javers.shadow.Shadow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JaversUtil {

  @Getter private static final Map<String, String> javersCommitPropertiesMap = new HashMap<>();
  private final MongoClient mongoClient;
  private final MessageSource messageSource;
  @Getter private Javers javers;

  @Value("${spring.data.mongodb.database}")
  private String databaseName;

  @Value("${javers.change.limit:500}")
  private int changeLimit;

  public static Map<String, String> clearPropertiesMap() {
    javersCommitPropertiesMap.clear();
    return javersCommitPropertiesMap;
  }

  public static void addJaversCommitProperties(String key, String value) {
    javersCommitPropertiesMap.put(key, value);
  }

  @PostConstruct
  public void init() {
    try {
      validateConfig();

      MongoRepository mongoRepository = new MongoRepository(mongoClient.getDatabase(databaseName));
      this.javers = JaversBuilder.javers().registerJaversRepository(mongoRepository).build();

      log.info("Javers initialized successfully using database '{}'", databaseName);
    } catch (Exception ex) {
      log.error(
          "Failed to initialize Javers for database '{}': {}", databaseName, ex.getMessage(), ex);
      throw new AuditServiceException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.JAVERS_INIT_FAILED,
          new Object[] {databaseName},
          messageSource);
    }
  }

  private void validateConfig() {
    if (databaseName == null || databaseName.isBlank()) {
      throw new AuditServiceException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.MISSING_DATABASE_NAME,
          null,
          messageSource);
    }
  }

  public AuditEventJavers getLastUpdatedVersion(
      String type, String typeId, Long commitId, int last) {
    JqlQuery jqlQuery =
        QueryBuilder.byClass(AuditEventJavers.class)
            .withChildValueObjects(true)
            .withCommitProperty("type", type)
            .withCommitProperty("typeId", typeId)
            .withScopeDeepPlus()
            .limit(changeLimit)
            .build();

    List<Shadow<AuditEventJavers>> shadows = javers.findShadows(jqlQuery);

    if (shadows == null || shadows.isEmpty()) {
      log.warn("No shadows found for type={}, typeId={}", type, typeId);
      return null;
    }

    if (commitId == null) {
      if (shadows.size() > last) {
        return shadows.get(shadows.size() - last).get();
      } else {
        return shadows.getLast().get();
      }
    }

    for (Shadow<AuditEventJavers> shadow : shadows) {
      if (shadow.getCommitId().getMajorId() == commitId) {
        return shadow.get();
      }
    }

    log.warn("No shadow found for type={}, typeId={}, commitId={}", type, typeId, commitId);
    return null;
  }

  public AuditEventJavers setPayloadInModel(
      AuditEvent auditEvent,
      AuditEventJavers auditEventJavers,
      JsonNode filteredNode,
      ObjectNode objectNode) {
    if (Objects.nonNull(auditEvent.fieldList())) {
      auditEventJavers.setJsonNode(updateFilteredNodeInRoot(filteredNode, objectNode));
    } else {
      auditEventJavers.setJsonNode(filteredNode);
    }
    return auditEventJavers;
  }

  private JsonNode updateFilteredNodeInRoot(JsonNode filteredNode, ObjectNode objectNode) {
    Iterator<String> fieldNameItr = filteredNode.fieldNames();

    while (fieldNameItr.hasNext()) {
      String fieldName = fieldNameItr.next();
      JsonNode filteredValue = filteredNode.get(fieldName);

      if (filteredValue.isObject()
          && objectNode.has(fieldName)
          && objectNode.get(fieldName).isObject()) {
        updateFilteredNodeInRoot(filteredValue, (ObjectNode) objectNode.get(fieldName));
      } else {
        objectNode.set(fieldName, filteredValue);
      }
    }
    return objectNode;
  }
}
