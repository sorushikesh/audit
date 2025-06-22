package com.sorushi.invoice.management.audit.repository.repositoryImpl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import java.util.Collections;
import java.util.List;
import org.javers.core.commit.CommitMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class CommitMetadataRepositoryImplTest {

  @Container
  static final GenericContainer<?> CONTAINER =
      new GenericContainer<>(DockerImageName.parse("alpine:3.19")).withCommand("sleep", "1");

  private MongoTemplate template;
  private JaversTTLConfig config;
  private CommitMetadataRepositoryImpl repo;

  @BeforeEach
  void setUp() {
    template = mock(MongoTemplate.class);
    config = mock(JaversTTLConfig.class);
    repo = new CommitMetadataRepositoryImpl(template, config);
  }

  @Test
  void findCommitMetadata() {
    when(template.find(any(Query.class), eq(CommitMetadata.class), anyString()))
        .thenReturn(Collections.emptyList());
    repo.findCommitMetadata(null, null, 10, 0);
    verify(template).find(any(Query.class), eq(CommitMetadata.class), anyString());
  }

  @Test
  void countCommitMetadata() {
    repo.countCommitMetadata(null, null);
    verify(template).count(any(Query.class), anyString());
  }
}
