package com.sorushi.invoice.management.audit.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.constants.Constants;
import java.time.Instant;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@Testcontainers
class TtlCleanupSchedulerContainerTest {

  @Container static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

  private MongoTemplate template;
  private TtlCleanupScheduler scheduler;

  @BeforeEach
  void setup() {
    template = spy(new MongoTemplate(MongoClients.create(MONGO.getConnectionString()), "test"));
    scheduler = new TtlCleanupScheduler(template);
  }

  @Test
  void removeExpiredDocumentsWithContainer() {
    long past = Instant.now().minusSeconds(3600).toEpochMilli();
    Document doc = new Document(Constants.FIELD_TTL_DATE, past);
    template.getDb().getCollection(Constants.COLLECTION_COMMIT_METADATA_COLLECTION).insertOne(doc);
    template.getDb().getCollection(Constants.COLLECTION_JV_SNAPSHOT).insertOne(new Document(Constants.FIELD_TTL_DATE, past));

    scheduler.removeExpiredDocuments();

    long commitCount =
        template.getDb().getCollection(Constants.COLLECTION_COMMIT_METADATA_COLLECTION).countDocuments();
    long snapshotCount =
        template.getDb().getCollection(Constants.COLLECTION_JV_SNAPSHOT).countDocuments();
    assertEquals(0, commitCount);
    assertEquals(0, snapshotCount);

    verify(template, times(1)).remove(any(Query.class), eq(Constants.COLLECTION_COMMIT_METADATA_COLLECTION));
    verify(template, times(1)).remove(any(Query.class), eq(Constants.COLLECTION_JV_SNAPSHOT));
  }
}
