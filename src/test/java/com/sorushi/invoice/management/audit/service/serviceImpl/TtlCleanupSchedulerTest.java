package com.sorushi.invoice.management.audit.service.serviceImpl;

import static org.mockito.Mockito.*;

import com.mongodb.client.result.DeleteResult;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import com.sorushi.invoice.management.audit.BaseContainerTest;

class TtlCleanupSchedulerTest extends BaseContainerTest {

  @Test
  void removeExpiredDocuments() {
    MongoTemplate template = mock(MongoTemplate.class);
    DeleteResult result = DeleteResult.acknowledged(1L);
    when(template.remove(any(Query.class), eq("commit_metadata"))).thenReturn(result);
    when(template.remove(any(Query.class), eq("jv_snapshots"))).thenReturn(result);
    TtlCleanupScheduler scheduler = new TtlCleanupScheduler(template);
    scheduler.removeExpiredDocuments();
    verify(template, times(1)).remove(any(Query.class), eq("commit_metadata"));
    verify(template, times(1)).remove(any(Query.class), eq("jv_snapshots"));
  }
}
