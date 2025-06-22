package com.sorushi.invoice.management.audit.service.serviceImpl;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.RemoveResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class TtlCleanupSchedulerTest {

  @Container
  static final GenericContainer<?> CONTAINER =
      new GenericContainer<>(DockerImageName.parse("alpine:3.19")).withCommand("sleep", "1");
  @Test
  void removeExpiredDocuments() {
    MongoTemplate template = mock(MongoTemplate.class);
    RemoveResult result = RemoveResult.fromDeletedCount(1L);
    when(template.remove(any(Query.class), eq("commit_metadata"))).thenReturn(result);
    when(template.remove(any(Query.class), eq("jv_snapshots"))).thenReturn(result);
    TtlCleanupScheduler scheduler = new TtlCleanupScheduler(template);
    scheduler.removeExpiredDocuments();
    verify(template, times(1)).remove(any(Query.class), eq("commit_metadata"));
    verify(template, times(1)).remove(any(Query.class), eq("jv_snapshots"));
  }
}
