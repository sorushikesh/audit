package com.sorushi.invoice.management.audit.repository.repositoryImpl;

import static com.sorushi.invoice.management.audit.constants.Constants.*;

import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import com.sorushi.invoice.management.audit.exception.ErrorCodes;
import com.sorushi.invoice.management.audit.repository.CommitMetadataRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.commit.CommitMetadata;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class CommitMetadataRepositoryImpl implements CommitMetadataRepository {

  private final MongoTemplate mongoTemplate;
  private final JaversTTLConfig javersTTLConfig;
  private final MessageSource messageSource;

  public CommitMetadataRepositoryImpl(
      MongoTemplate mongoTemplate, JaversTTLConfig javersTTLConfig, MessageSource messageSource) {
    this.mongoTemplate = mongoTemplate;
    this.javersTTLConfig = javersTTLConfig;
    this.messageSource = messageSource;
  }

  @Override
  public void saveCommitMetadata(CommitMetadata commitMetadata) {
    log.info(
        "Attempting to save commit metadata for author: {}, commitDate: {}",
        commitMetadata.getAuthor(),
        commitMetadata.getCommitDate());

    try {
      CommitMetadata savedCommit =
          mongoTemplate.save(commitMetadata, COLLECTION_COMMIT_METADATA_COLLECTION);
      log.info("Successfully saved commit metadata with ID: {}", savedCommit.getId());

      Instant commitTtlDate =
          Instant.now().plus(javersTTLConfig.getCommitMetadataDays(), ChronoUnit.DAYS);
      Update commitUpdate = new Update().set(FIELD_TTL_DATE, commitTtlDate.toEpochMilli());

      long updatedCommitDocs =
          mongoTemplate
              .updateMulti(
                  Query.query(
                      Criteria.where(FIELD_UNDERSCORE_ID + DOT + FIELD_MAJOR_ID)
                          .is(savedCommit.getId().getMajorId())),
                  commitUpdate,
                  COLLECTION_COMMIT_METADATA_COLLECTION)
              .getModifiedCount();
      log.info("Updated TTL date for {} commit metadata documents", updatedCommitDocs);

      Instant snapshotTtlDate =
          Instant.now().plus(javersTTLConfig.getSnapshotDays(), ChronoUnit.DAYS);
      Update snapshotUpdate = new Update().set(FIELD_TTL_DATE, snapshotTtlDate.toEpochMilli());

      long updatedSnapshotDocs =
          mongoTemplate
              .updateMulti(
                  Query.query(
                      Criteria.where(FIELD_COMMIT_META_DATA + DOT + FIELD_ID)
                          .is(savedCommit.getId().getMajorId())),
                  snapshotUpdate,
                  COLLECTION_JV_SNAPSHOT)
              .getModifiedCount();
      log.info("Updated TTL date for {} snapshot documents", updatedSnapshotDocs);

    } catch (Exception e) {
      log.error("Failed to save commit metadata: {}", commitMetadata, e);
      throw new AuditServiceException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          ErrorCodes.COMMIT_METADATA_SAVE_FAILED,
          new Object[] {commitMetadata},
          messageSource);
    }
  }

  @Override
  public List<CommitMetadata> findCommitMetadata(
      Date startDate, Date endDate, int limit, int skip) {
    log.info(
        "Fetching commit metadata with filters - startDate: {}, endDate: {}, limit: {}, skip: {}",
        startDate,
        endDate,
        limit,
        skip);

    Query query = new Query();
    if (startDate != null || endDate != null) {
      Criteria dateCriteria = Criteria.where(FIELD_COMMIT_DATE);
      if (startDate != null) {
        dateCriteria = dateCriteria.gte(startDate);
      }
      if (endDate != null) {
        dateCriteria = dateCriteria.lte(endDate);
      }
      query.addCriteria(dateCriteria);
    }

    if (skip > 0) query.skip(skip);
    if (limit > 0) query.limit(limit);

    List<CommitMetadata> commits =
        mongoTemplate.find(query, CommitMetadata.class, COLLECTION_COMMIT_METADATA_COLLECTION);
    log.info("Found {} commit metadata documents", commits.size());
    return commits;
  }

  @Override
  public long countCommitMetadata(Date startDate, Date endDate) {
    log.info(
        "Counting commit metadata with filters - startDate: {}, endDate: {}", startDate, endDate);

    Query query = new Query();
    if (startDate != null || endDate != null) {
      Criteria dateCriteria = Criteria.where(FIELD_COMMIT_DATE);
      if (startDate != null) {
        dateCriteria = dateCriteria.gte(startDate);
      }
      if (endDate != null) {
        dateCriteria = dateCriteria.lte(endDate);
      }
      query.addCriteria(dateCriteria);
    }

    long count = mongoTemplate.count(query, COLLECTION_COMMIT_METADATA_COLLECTION);
    log.info("Counted {} commit metadata documents", count);
    return count;
  }

  @Override
  public List<CommitMetadata> findCommitMetadataByEntity(
      String entityType, String entityId, Date startDate, Date endDate, int limit, int skip) {

    log.info(
        "Fetching commit metadata for entity [{}:{}] with date range {} - {}, limit: {}, skip: {}",
        entityType,
        entityId,
        startDate,
        endDate,
        limit,
        skip);

    Criteria criteria =
        Criteria.where(FIELD_PROPERTIES + DOT + TYPE)
            .is(entityType)
            .and(FIELD_PROPERTIES + DOT + TYPE_ID)
            .is(entityId);

    if (startDate != null || endDate != null) {
      Criteria dateCriteria = Criteria.where(FIELD_COMMIT_DATE);
      if (startDate != null) {
        dateCriteria = dateCriteria.gte(startDate);
      }
      if (endDate != null) {
        dateCriteria = dateCriteria.lte(endDate);
      }
      criteria = new Criteria().andOperator(criteria, dateCriteria);
    }

    Query query = new Query(criteria);
    if (skip > 0) query.skip(skip);
    if (limit > 0) query.limit(limit);

    List<CommitMetadata> commits =
        mongoTemplate.find(query, CommitMetadata.class, COLLECTION_COMMIT_METADATA_COLLECTION);
    log.info(
        "Found {} commit metadata documents for [{}:{}]", commits.size(), entityType, entityId);
    return commits;
  }

  @Override
  public long countCommitMetadataByEntity(
      String entityType, String entityId, Date startDate, Date endDate) {
    log.info(
        "Counting commit metadata for entity [{}:{}] with date range {} - {}",
        entityType,
        entityId,
        startDate,
        endDate);

    Criteria criteria =
        Criteria.where(FIELD_PROPERTIES + DOT + TYPE)
            .is(entityType)
            .and(FIELD_PROPERTIES + DOT + TYPE_ID)
            .is(entityId);

    if (startDate != null || endDate != null) {
      Criteria dateCriteria = Criteria.where(FIELD_COMMIT_DATE);
      if (startDate != null) {
        dateCriteria = dateCriteria.gte(startDate);
      }
      if (endDate != null) {
        dateCriteria = dateCriteria.lte(endDate);
      }
      criteria = new Criteria().andOperator(criteria, dateCriteria);
    }

    Query query = new Query(criteria);
    long count = mongoTemplate.count(query, COLLECTION_COMMIT_METADATA_COLLECTION);
    log.info("Counted {} commit metadata documents for [{}:{}]", count, entityType, entityId);
    return count;
  }

  @Override
  public List<CommitMetadata> findCommitMetadataByUser(
      String userId, Date startDate, Date endDate, int limit, int skip) {

    log.info(
        "Fetching commit metadata for user {} with date range {} - {}, limit: {}, skip: {}",
        userId,
        startDate,
        endDate,
        limit,
        skip);

    Criteria criteria = Criteria.where(FIELD_PROPERTIES + DOT + USER_ID).is(userId);

    if (startDate != null || endDate != null) {
      Criteria dateCriteria = Criteria.where(FIELD_COMMIT_DATE);
      if (startDate != null) {
        dateCriteria = dateCriteria.gte(startDate);
      }
      if (endDate != null) {
        dateCriteria = dateCriteria.lte(endDate);
      }
      criteria = new Criteria().andOperator(criteria, dateCriteria);
    }

    Query query = new Query(criteria);
    if (skip > 0) query.skip(skip);
    if (limit > 0) query.limit(limit);

    List<CommitMetadata> commits =
        mongoTemplate.find(query, CommitMetadata.class, COLLECTION_COMMIT_METADATA_COLLECTION);
    log.info("Found {} commit metadata documents for user {}", commits.size(), userId);
    return commits;
  }

  @Override
  public long countCommitMetadataByUser(String userId, Date startDate, Date endDate) {
    log.info(
        "Counting commit metadata for user {} with date range {} - {}", userId, startDate, endDate);

    Criteria criteria = Criteria.where(FIELD_PROPERTIES + DOT + USER_ID).is(userId);

    if (startDate != null || endDate != null) {
      Criteria dateCriteria = Criteria.where(FIELD_COMMIT_DATE);
      if (startDate != null) {
        dateCriteria = dateCriteria.gte(startDate);
      }
      if (endDate != null) {
        dateCriteria = dateCriteria.lte(endDate);
      }
      criteria = new Criteria().andOperator(criteria, dateCriteria);
    }

    Query query = new Query(criteria);
    long count = mongoTemplate.count(query, COLLECTION_COMMIT_METADATA_COLLECTION);
    log.info("Counted {} commit metadata documents for user {}", count, userId);
    return count;
  }
}
