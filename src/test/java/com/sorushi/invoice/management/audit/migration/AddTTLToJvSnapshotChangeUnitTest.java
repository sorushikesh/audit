package com.sorushi.invoice.management.audit.migration;

import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

class AddTTLToJvSnapshotChangeUnitTest extends BaseContainerTest {

  @Test
  void executeAndRollback() {
    MongoTemplate template = mock(MongoTemplate.class);
    JaversTTLConfig config = mock(JaversTTLConfig.class);
    when(config.getSnapshotDays()).thenReturn(1);
    AddTTLToJvSnapshotChangeUnit unit = new AddTTLToJvSnapshotChangeUnit(template, config);
    unit.execute();
    verify(template).updateMulti(any(), any(Update.class), anyString());
    unit.rollback();
    verify(template, times(2)).updateMulti(any(), any(Update.class), anyString());
  }
}
