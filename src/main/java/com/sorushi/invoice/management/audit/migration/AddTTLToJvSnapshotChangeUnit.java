package com.sorushi.invoice.management.audit.migration;

import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
    Update update = new Update().set("TTL_DATE", Date.from(ttlDate));
    mongoTemplate.updateMulti(new Query(), update, "jv_snapshots");
  }

  @RollbackExecution
  public void rollback() {
    Update update = new Update().unset("TTL_DATE");
    mongoTemplate.updateMulti(new Query(), update, "jv_snapshots");
  }
}
