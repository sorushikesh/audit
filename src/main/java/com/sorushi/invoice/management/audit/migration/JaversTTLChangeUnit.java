package com.sorushi.invoice.management.audit.migration;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@ChangeUnit(id = "javers-ttl-indexes", order = "001", author = "codex")
@RequiredArgsConstructor
public class JaversTTLChangeUnit {

  private final MongoTemplate mongoTemplate;

  @Value("${javers.ttl.commit-metadata-days:30}")
  private int commitMetadataDays;

  @Value("${javers.ttl.snapshot-days:30}")
  private int snapshotDays;

  @Execution
  public void addTTLIndexes() {
    long commitSeconds = commitMetadataDays * 24L * 3600L;
    long snapshotSeconds = snapshotDays * 24L * 3600L;

    IndexOperations commitOps = mongoTemplate.indexOps("commit_metadata");
    commitOps.ensureIndex(
        new Index().on("commitDateInstant", Sort.Direction.ASC).expire(commitSeconds));

    IndexOperations snapshotOps = mongoTemplate.indexOps("jv_snapshot");
    snapshotOps.ensureIndex(
        new Index().on("commitMetadata.commitDateInstant", Sort.Direction.ASC).expire(snapshotSeconds));
  }

  @RollbackExecution
  public void rollback() {
    IndexOperations commitOps = mongoTemplate.indexOps("commit_metadata");
    commitOps.dropIndex("commitDateInstant_1");
    IndexOperations snapshotOps = mongoTemplate.indexOps("jv_snapshot");
    snapshotOps.dropIndex("commitMetadata.commitDateInstant_1");
  }
}
