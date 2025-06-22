package com.sorushi.invoice.management.audit.migration;

import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class AddTTLToJvSnapshotChangeUnitTest {

  @Container
  static final GenericContainer<?> CONTAINER =
      new GenericContainer<>(DockerImageName.parse("alpine:3.19")).withCommand("sleep", "1");

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
