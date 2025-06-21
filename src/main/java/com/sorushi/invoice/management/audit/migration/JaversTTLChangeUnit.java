package com.sorushi.invoice.management.audit.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@ChangeUnit(id = "javers-ttl-indexes", order = "002", author = "Rushikesh")
@RequiredArgsConstructor
public class JaversTTLChangeUnit {

  private static final String COMMIT_METADATA_COLLECTION = "commit_metadata";
  private static final String SNAPSHOT_COLLECTION = "jv_snapshot";
  private static final String COMMIT_DATE_FIELD = "commitDateInstant";
  private static final String TTL_DATE_FIELD = "TTL_DATE";
  private static final String COMMIT_DATE_INDEX_NAME = "TTL_DATE_commit_metadata_1";
  private static final String SNAPSHOT_COMMIT_DATE_INDEX_NAME = "TTL_DATE_jv_snapshot_1";

  private final MongoTemplate mongoTemplate;

  @Value("${javers.ttl.commit-metadata-days:30}")
  private int commitMetadataDays;

  @Value("${javers.ttl.snapshot-days:30}")
  private int snapshotDays;

  @Execution
  public void addTTLIndexes() {

    // 1. Update TTL_DATE field in commit_metadata
    mongoTemplate
        .findAll(Document.class, COMMIT_METADATA_COLLECTION)
        .forEach(
            doc -> {
              Date commitDate = doc.getDate(COMMIT_DATE_FIELD);
              if (commitDate != null) {
                Date ttlDate =
                    Date.from(commitDate.toInstant().plus(commitMetadataDays, ChronoUnit.DAYS));
                mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(doc.getObjectId("_id"))),
                    new Update().set(TTL_DATE_FIELD, ttlDate),
                    COMMIT_METADATA_COLLECTION);
              }
            });

    // 2. Update TTL_DATE field in jv_snapshot
    mongoTemplate
        .findAll(Document.class, SNAPSHOT_COLLECTION)
        .forEach(
            doc -> {
              Document commitMetadata = (Document) doc.get("commitMetadata");
              if (commitMetadata != null && commitMetadata.getDate(COMMIT_DATE_FIELD) != null) {
                Date commitDate = commitMetadata.getDate(COMMIT_DATE_FIELD);
                Date ttlDate =
                    Date.from(commitDate.toInstant().plus(snapshotDays, ChronoUnit.DAYS));
                mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(doc.getObjectId("_id"))),
                    new Update().set(TTL_DATE_FIELD, ttlDate),
                    SNAPSHOT_COLLECTION);
              }
            });

    // 3. Create TTL indexes on TTL_DATE field
    Index commitTTLIndex =
        new Index()
            .on(TTL_DATE_FIELD, Sort.Direction.ASC)
            .named(COMMIT_DATE_INDEX_NAME)
            .expire(0); // TTL countdown starts from the value in the TTL_DATE field

    Index snapshotTTLIndex =
        new Index()
            .on(TTL_DATE_FIELD, Sort.Direction.ASC)
            .named(SNAPSHOT_COMMIT_DATE_INDEX_NAME)
            .expire(0); // same here

    mongoTemplate.indexOps(COMMIT_METADATA_COLLECTION).createIndex(commitTTLIndex);
    mongoTemplate.indexOps(SNAPSHOT_COLLECTION).createIndex(snapshotTTLIndex);
  }

  @RollbackExecution
  public void rollback() {
    // Remove TTL indexes
    mongoTemplate.indexOps(COMMIT_METADATA_COLLECTION).dropIndex(COMMIT_DATE_INDEX_NAME);
    mongoTemplate.indexOps(SNAPSHOT_COLLECTION).dropIndex(SNAPSHOT_COMMIT_DATE_INDEX_NAME);

    // Optionally: remove TTL_DATE fields (comment out if rollback should not touch data)
    mongoTemplate.updateMulti(
        new Query(), new Update().unset(TTL_DATE_FIELD), COMMIT_METADATA_COLLECTION);
    mongoTemplate.updateMulti(new Query(), new Update().unset(TTL_DATE_FIELD), SNAPSHOT_COLLECTION);
  }
}
