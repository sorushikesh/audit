package com.sorushi.invoice.management.audit.service.serviceImpl;

import static com.sorushi.invoice.management.audit.constants.Constants.COLLECTION_COMMIT_METADATA_COLLECTION;
import static com.sorushi.invoice.management.audit.constants.Constants.COLLECTION_JV_SNAPSHOT;
import static com.sorushi.invoice.management.audit.constants.Constants.FIELD_TTL_DATE;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TtlCleanupScheduler {

  private final MongoTemplate mongoTemplate;

  @PostConstruct
  @Scheduled(cron = "0 0 * * * *")
  public void removeExpiredDocuments() {
    long now = Instant.now().toEpochMilli();
    Query expiredQuery = Query.query(Criteria.where(FIELD_TTL_DATE).lte(now));

    long deletedCommits =
        mongoTemplate.remove(expiredQuery, COLLECTION_COMMIT_METADATA_COLLECTION).getDeletedCount();
    long deletedSnapshots =
        mongoTemplate.remove(expiredQuery, COLLECTION_JV_SNAPSHOT).getDeletedCount();

    if (deletedCommits > 0 || deletedSnapshots > 0) {
      log.info(
          "Deleted {} expired commit metadata and {} expired snapshots",
          deletedCommits,
          deletedSnapshots);
    }
  }
}
