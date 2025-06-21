package com.sorushi.invoice.management.audit.migration;

import static com.sorushi.invoice.management.audit.constants.Constants.COLLECTION_JV_SNAPSHOT;
import static com.sorushi.invoice.management.audit.constants.Constants.FIELD_TTL_DATE;

import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Slf4j
@ChangeUnit(id = "add-ttl-date-to-jv-snapshot", order = "002", author = "Rushikesh")
@RequiredArgsConstructor
@Component
public class AddTTLToJvSnapshotChangeUnit {

  private final MongoTemplate mongoTemplate;
  private final JaversTTLConfig javersTTLConfig;

  @Execution
  public void execute() {

    int snapshotDays = javersTTLConfig.getSnapshotDays();
    Instant ttlDate = Instant.now().plus(snapshotDays, ChronoUnit.DAYS);
    Update update = new Update().set(FIELD_TTL_DATE, Date.from(ttlDate));
    mongoTemplate.updateMulti(new Query(), update, COLLECTION_JV_SNAPSHOT);
  }

  @RollbackExecution
  public void rollback() {
    Update update = new Update().unset(FIELD_TTL_DATE);
    mongoTemplate.updateMulti(new Query(), update, COLLECTION_JV_SNAPSHOT);
  }
}
