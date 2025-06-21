package com.sorushi.invoice.management.audit.repository.repositoryImpl;

import static com.sorushi.invoice.management.audit.constants.Constants.COLLECTION_COMMIT_METADATA_COLLECTION;
import static com.sorushi.invoice.management.audit.constants.Constants.COLLECTION_JV_SNAPSHOT;
import static com.sorushi.invoice.management.audit.constants.Constants.FIELD_TTL_DATE;

import com.sorushi.invoice.management.audit.repository.CommitMetadataRepository;
import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.commit.CommitMetadata;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Repository
public class CommitMetadataRepositoryImpl implements CommitMetadataRepository {

  private final MongoTemplate mongoTemplate;
  private final JaversTTLConfig javersTTLConfig;

  public CommitMetadataRepositoryImpl(MongoTemplate mongoTemplate, JaversTTLConfig javersTTLConfig) {
    this.mongoTemplate = mongoTemplate;
    this.javersTTLConfig = javersTTLConfig;
  }

  @Override
  public void saveCommitMetadata(CommitMetadata commitMetadata) {
    log.info(
        "Saving commit metadata: {} in collection: {}",
        commitMetadata,
        COLLECTION_COMMIT_METADATA_COLLECTION);
    try {
      CommitMetadata savedCommit =
          mongoTemplate.save(commitMetadata, COLLECTION_COMMIT_METADATA_COLLECTION);
      log.debug("Successfully saved commit metadata with ID: {}", savedCommit.getId());

      Instant commitTtlDate =
          Instant.now().plus(javersTTLConfig.getCommitMetadataDays(), ChronoUnit.DAYS);
      Update commitUpdate = new Update().set(FIELD_TTL_DATE, Date.from(commitTtlDate));
      mongoTemplate.updateMulti(
          Query.query(Criteria.where("_id.majorId").is(savedCommit.getId().getMajorId())),
          commitUpdate,
          COLLECTION_COMMIT_METADATA_COLLECTION);

      Instant snapshotTtlDate =
          Instant.now().plus(javersTTLConfig.getSnapshotDays(), ChronoUnit.DAYS);
      Update snapshotUpdate = new Update().set(FIELD_TTL_DATE, Date.from(snapshotTtlDate));
      mongoTemplate.updateMulti(
          Query.query(Criteria.where("commitMetadata.id").is(savedCommit.getId().getMajorId())),
          snapshotUpdate,
          COLLECTION_JV_SNAPSHOT);
    } catch (Exception e) {
      log.error("Failed to save commit metadata: {}", commitMetadata, e);
      throw e;
    }
  }

  @Override
  public java.util.List<CommitMetadata> findAllCommitMetadata() {
    return mongoTemplate.findAll(CommitMetadata.class, COLLECTION_COMMIT_METADATA_COLLECTION);
  }

  @Override
  public java.util.List<CommitMetadata> findCommitMetadataByEntity(
      String entityType, String entityId) {
    Query query =
        new Query(Criteria.where("properties.type")
                .is(entityType)
                .and("properties.typeId")
                .is(entityId));
    return mongoTemplate.find(query, CommitMetadata.class, COLLECTION_COMMIT_METADATA_COLLECTION);
  }
}
