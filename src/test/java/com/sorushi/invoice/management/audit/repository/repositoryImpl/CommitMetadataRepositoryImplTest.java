package com.sorushi.invoice.management.audit.repository.repositoryImpl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.configuration.JaversTTLConfig;
import java.util.Collections;
import org.javers.core.commit.CommitMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

class CommitMetadataRepositoryImplTest extends BaseContainerTest {

  private MongoTemplate template;
  private JaversTTLConfig config;
  private CommitMetadataRepositoryImpl repo;

  @BeforeEach
  void setUp() {
    template = mock(MongoTemplate.class);
    config = mock(JaversTTLConfig.class);
    repo = new CommitMetadataRepositoryImpl(template, config, mock(org.springframework.context.MessageSource.class));
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

  @Test
  void findCommitMetadataByUser() {
    when(template.find(any(Query.class), eq(CommitMetadata.class), anyString()))
        .thenReturn(Collections.emptyList());
    repo.findCommitMetadataByUser("u1", null, null, 5, 0);
    verify(template).find(any(Query.class), eq(CommitMetadata.class), anyString());
  }

  @Test
  void countCommitMetadataByUser() {
    repo.countCommitMetadataByUser("u1", null, null);
    verify(template).count(any(Query.class), anyString());
  }
}
