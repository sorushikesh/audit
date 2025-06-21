package com.sorushi.invoice.management.audit.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
@RequiredArgsConstructor
public class MongoTTLConfig {

  private final MongoTemplate mongoTemplate;

  @Value("${javers.ttl.commit-metadata-days:30}")
  private int commitMetadataDays;

  @Value("${javers.ttl.snapshot-days:30}")
  private int snapshotDays;

  @PostConstruct
  public void ensureTTLIndexes() {
    long commitSeconds = commitMetadataDays * 24L * 3600L;
    long snapshotSeconds = snapshotDays * 24L * 3600L;

    IndexOperations commitOps = mongoTemplate.indexOps("commit_metadata");
    commitOps.ensureIndex(
        new Index().on("commitDateInstant", Sort.Direction.ASC).expire(commitSeconds));

    IndexOperations snapshotOps = mongoTemplate.indexOps("jv_snapshot");
    snapshotOps.ensureIndex(
        new Index().on("commitMetadata.commitDateInstant", Sort.Direction.ASC).expire(snapshotSeconds));
  }
}
