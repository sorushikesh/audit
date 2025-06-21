package com.sorushi.invoice.management.audit.migration;

import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@RequiredArgsConstructor
@ChangeUnit(id = "add-ttl-date-to-commit-metadata", order = "001", author = "Rushikesh")
@Slf4j
public class AddTTLToCommitMetadataChangeUnit {

  private final MongoTemplate mongoTemplate;
  private final JaversTTLConfig javersTTLConfig;

  @Execution
  public void execute() {

    int commitMetadataDays = javersTTLConfig.getCommitMetadataDays();
    Instant ttlDate = Instant.now().plus(commitMetadataDays, ChronoUnit.DAYS);
    Update update = new Update().set("TTL_DATE", Date.from(ttlDate));
    mongoTemplate.updateMulti(new Query(), update, "commit_metadata");
  }

  @RollbackExecution
  public void rollback() {
    Update update = new Update().unset("TTL_DATE");
    mongoTemplate.updateMulti(new Query(), update, "commit_metadata");
  }
}

