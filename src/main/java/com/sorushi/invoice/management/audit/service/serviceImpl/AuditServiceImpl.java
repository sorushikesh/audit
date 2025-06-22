package com.sorushi.invoice.management.audit.service.serviceImpl;

import static com.sorushi.invoice.management.audit.constants.Constants.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.dto.AuditEventsQuery;
import com.sorushi.invoice.management.audit.dto.AuditEventsResponse;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import com.sorushi.invoice.management.audit.model.AuditEventJavers;
import com.sorushi.invoice.management.audit.model.AuditEventView;
import com.sorushi.invoice.management.audit.repository.repositoryImpl.CommitMetadataRepositoryImpl;
import com.sorushi.invoice.management.audit.service.AuditService;
import com.sorushi.invoice.management.audit.util.AuditHelperUtil;
import com.sorushi.invoice.management.audit.util.JaversUtil;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
  public AuditEvent processAuditEvent(AuditEvent auditEvent)
      throws JsonProcessingException, AuditServiceException {

    log.info(
        "Starting audit processing for entityType='{}', entityId='{}', operation='{}'",
        auditEvent.entityType(),
        auditEvent.entityId(),
        auditEvent.operation());

    auditHelperUtil.validateDate(
        auditEvent.changedDate(),
        List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME, DateTimeFormatter.ISO_DATE_TIME));
    log.info("Date validated for audit event: {}", auditEvent.changedDate());

    List<String> fieldListInRequest = new ArrayList<>();
    auditHelperUtil.filterRequestFields(auditEvent, fieldListInRequest);
    log.info("Fields filtered from request: {}", fieldListInRequest);

    auditHelperUtil.processFieldListIfPresentInRequest(auditEvent, fieldListInRequest);
    log.info("Fields processed and updated in request");

    Javers javers = javersUtil.getJavers();
    log.info("Javers instance created");

    Map<String, Object> newVal = auditEvent.newVal();
    String author = Optional.ofNullable(auditEvent.author()).orElse(DEFAULT_USER);
    Map<String, String> commitProperties = auditHelperUtil.addCommitPropertiesMap(auditEvent);

    Commit commit;
    JsonNode newJsonNode;

    if (!OPERATION_READ.equalsIgnoreCase(auditEvent.operation())) {
      log.info("Processing non-read operation: {}", auditEvent.operation());
      newJsonNode = objectMapper.readTree(javers.getJsonConverter().toJson(newVal));

      AuditEventJavers previousVersion =
          javersUtil.getLastUpdatedVersion(auditEvent.entityType(), auditEvent.entityId(), null, 0);

      ObjectNode originalNode;
      if (previousVersion == null || previousVersion.getJsonNode() == null) {
        log.info("No previous version found. Creating new version.");
        previousVersion = AuditEventJavers.builder().id(auditEvent.entityId()).build();
        originalNode = objectMapper.createObjectNode();
      } else {
        originalNode = previousVersion.getJsonNode().deepCopy();
        log.info("Previous version loaded with existing payload");
      }

      previousVersion =
          javersUtil.setPayloadInModel(auditEvent, previousVersion, newJsonNode, originalNode);

      if (OPERATION_DELETE.equalsIgnoreCase(commitProperties.get(OPERATION))) {
        previousVersion.setJsonNode(null);
        log.info("Cleared jsonNode due to delete operation");
      }

      commit = javers.commit(author, previousVersion, commitProperties);
    } else {
      log.info("Processing read operation");
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

    return auditEvent;
  }

  @Override
  public AuditEventsResponse fetchAuditData(AuditEventsQuery query) {
    log.info("Fetching audit data with query: {}", query);

    Date startDate = parseDate(query.startDate());
    Date endDate = parseDate(query.endDate());
    int limit = Optional.ofNullable(query.limit()).orElse(0);
    int skip = Optional.ofNullable(query.skip()).orElse(0);

    List<CommitMetadata> records =
        commitMetadataRepository.findCommitMetadata(startDate, endDate, limit, skip);
    long count = commitMetadataRepository.countCommitMetadata(startDate, endDate);

    log.info("Fetched {} records out of total {}", records.size(), count);

    return AuditEventsResponse.builder()
        .result(RESPONSE_RESULT_SUCCESS)
        .count(count)
        .records(records)
        .build();
  }

  @Override
  public AuditEventsResponse fetchAuditDataForEntity(
      String entityType, String entityId, AuditEventsQuery query) {
    log.info(
        "Fetching audit data for entityType='{}', entityId='{}', query={}",
        entityType,
        entityId,
        query);

    Date startDate = parseDate(query.startDate());
    Date endDate = parseDate(query.endDate());
    int limit = Optional.ofNullable(query.limit()).orElse(0);
    int skip = Optional.ofNullable(query.skip()).orElse(0);

    List<CommitMetadata> records =
        commitMetadataRepository.findCommitMetadataByEntity(
            entityType, entityId, startDate, endDate, limit, skip);
    long count =
        commitMetadataRepository.countCommitMetadataByEntity(
            entityType, entityId, startDate, endDate);

    log.info(
        "Fetched {} records for [{}:{}], total count: {}",
        records.size(),
        entityType,
        entityId,
        count);

    return AuditEventsResponse.builder()
        .result(RESPONSE_RESULT_SUCCESS)
        .count(count)
        .records(records)
        .build();
  }

  @Override
  public AuditEventsResponse fetchAuditDataForUser(String userId, AuditEventsQuery query) {
    log.info("Fetching audit data for user '{}', query={}", userId, query);

    Date startDate = parseDate(query.startDate());
    Date endDate = parseDate(query.endDate());
    int limit = Optional.ofNullable(query.limit()).orElse(0);
    int skip = Optional.ofNullable(query.skip()).orElse(0);

    List<CommitMetadata> records =
        commitMetadataRepository.findCommitMetadataByUser(userId, startDate, endDate, limit, skip);
    long count = commitMetadataRepository.countCommitMetadataByUser(userId, startDate, endDate);

    log.info("Fetched {} records for user {}, total count: {}", records.size(), userId, count);

    return AuditEventsResponse.builder()
        .result(RESPONSE_RESULT_SUCCESS)
        .count(count)
        .records(records)
        .build();
  }

  private Date parseDate(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }

    log.info("Parsing date string: {}", dateStr);
    auditHelperUtil.validateDate(
        dateStr, List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME, DateTimeFormatter.ISO_DATE_TIME));

    try {
      Date parsedDate =
          Date.from(
              LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                  .atZone(ZoneId.systemDefault())
                  .toInstant());
      log.info("Parsed date: {}", parsedDate);
      return parsedDate;
    } catch (Exception e1) {
      try {
        Date parsedDate =
            Date.from(
                OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
                    .toInstant());
        log.info("Parsed date with offset: {}", parsedDate);
        return parsedDate;
      } catch (Exception e2) {
        log.warn("Failed to parse date: {}. Returning null.", dateStr, e2);
        return null;
      }
    }
  }
}
