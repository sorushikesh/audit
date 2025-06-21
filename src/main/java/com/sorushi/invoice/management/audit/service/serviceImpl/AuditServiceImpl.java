package com.sorushi.invoice.management.audit.service.serviceImpl;

import static com.sorushi.invoice.management.audit.constants.Constants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import com.sorushi.invoice.management.audit.model.AuditEventJavers;
import com.sorushi.invoice.management.audit.model.AuditEventView;
import com.sorushi.invoice.management.audit.repository.repositoryImpl.CommitMetadataRepositoryImpl;
import com.sorushi.invoice.management.audit.service.AuditService;
import com.sorushi.invoice.management.audit.util.AuditHelperUtil;
import com.sorushi.invoice.management.audit.util.JaversUtil;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.Javers;
import org.javers.core.commit.Commit;
import org.javers.core.commit.CommitMetadata;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

  private final CommitMetadataRepositoryImpl commitMetadataRepository;
  private final AuditHelperUtil auditHelperUtil;
  private final JaversUtil javersUtil;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AuditServiceImpl(
      CommitMetadataRepositoryImpl commitMetadataRepository,
      AuditHelperUtil auditHelperUtil,
      JaversUtil javersUtil) {
    this.commitMetadataRepository = commitMetadataRepository;
    this.auditHelperUtil = auditHelperUtil;
    this.javersUtil = javersUtil;
  }

  @Override
  public void processAuditEvent(AuditEvent auditEvent)
      throws JsonProcessingException, AuditServiceException {

    log.info("Validating audit event at: {}", auditEvent.changedDate());
    auditHelperUtil.validateDate(
        auditEvent.changedDate(),
        List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME, DateTimeFormatter.ISO_DATE_TIME));

    List<String> fieldListInRequest = new ArrayList<>();

    log.info("Filtering fields from request...");
    auditHelperUtil.filterRequestFields(auditEvent, fieldListInRequest);

    log.info("Processing requested field list...");
    auditHelperUtil.processFieldListIfPresentInRequest(auditEvent, fieldListInRequest);

    log.info("Building Javers instance...");
    Javers javers = javersUtil.getJavers();

    Map<String, Object> newVal = auditEvent.newVal();
    String author = Optional.ofNullable(auditEvent.author()).orElse(DEFAULT_USER);

    Map<String, String> commitProperties = auditHelperUtil.addCommitPropertiesMap(auditEvent);

    Commit commit;
    JsonNode newJsonNode;

    if (!OPERATION_READ.equalsIgnoreCase(auditEvent.operation())) {
      newJsonNode = objectMapper.readTree(javers.getJsonConverter().toJson(newVal));

      AuditEventJavers previousVersion =
          javersUtil.getLastUpdatedVersion(auditEvent.entityType(), auditEvent.entityId(), null, 0);

      ObjectNode originalNode;

      if (previousVersion == null || previousVersion.getJsonNode() == null) {
        previousVersion = AuditEventJavers.builder().id(auditEvent.entityId()).build();
        originalNode = objectMapper.createObjectNode();
      } else {
        originalNode = previousVersion.getJsonNode().deepCopy();
      }

      previousVersion =
          javersUtil.setPayloadInModel(auditEvent, previousVersion, newJsonNode, originalNode);

      if (OPERATION_DELETE.equalsIgnoreCase(commitProperties.get(OPERATION))) {
        previousVersion.setJsonNode(null);
      }

      commit = javers.commit(author, previousVersion, commitProperties);

    } else {
      AuditEventView auditEventView =
          AuditEventView.builder().id(UUID.randomUUID().toString()).build();
      commit = javers.commit(author, auditEventView, commitProperties);
    }

    CommitMetadata commitMetadata =
        new CommitMetadata(
            commit.getAuthor(),
            commit.getProperties(),
            commit.getCommitDate(),
            commit.getCommitDateInstant(),
            commit.getId());

    commitMetadataRepository.saveCommitMetadata(commitMetadata);
    fieldListInRequest.clear();

    log.info(
        "Audit committed successfully for entityType='{}', entityId='{}'",
        auditEvent.entityType(),
        auditEvent.entityId());
  }
}
