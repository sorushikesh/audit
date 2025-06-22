package com.sorushi.invoice.management.audit.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.dto.AuditEventsQuery;
import com.sorushi.invoice.management.audit.dto.AuditEventsResponse;
import com.sorushi.invoice.management.audit.repository.repositoryImpl.CommitMetadataRepositoryImpl;
import com.sorushi.invoice.management.audit.util.AuditHelperUtil;
import com.sorushi.invoice.management.audit.util.JaversUtil;
import java.util.Collections;
import java.util.List;
import org.javers.core.commit.CommitMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuditServiceImplTest extends BaseContainerTest {

  private CommitMetadataRepositoryImpl repo;
  private AuditHelperUtil helper;
  private JaversUtil javers;
  private AuditServiceImpl service;

  @BeforeEach
  void setUp() {
    repo = mock(CommitMetadataRepositoryImpl.class);
    helper = mock(AuditHelperUtil.class);
    javers = mock(JaversUtil.class);
    service = new AuditServiceImpl(repo, helper, javers);
  }

  @Test
  void fetchAuditDataParsesDates() {
    AuditEventsQuery query =
        new AuditEventsQuery(10, 0, "2024-01-01T00:00:00", "2024-01-02T00:00:00");
    when(repo.findCommitMetadata(any(), any(), anyInt(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(repo.countCommitMetadata(any(), any())).thenReturn(0L);

    AuditEventsResponse resp = service.fetchAuditData(query);
    assertEquals(0, resp.count());
    verify(repo).findCommitMetadata(any(), any(), eq(10), eq(0));
  }

  @Test
  void fetchAuditDataWithBadDates() {
    AuditEventsQuery query = new AuditEventsQuery(1, 0, "bad", null);
    when(repo.findCommitMetadata(isNull(), isNull(), anyInt(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(repo.countCommitMetadata(isNull(), isNull())).thenReturn(0L);
    AuditEventsResponse resp = service.fetchAuditData(query);
    assertEquals(0, resp.count());
  }

  @Test
  void fetchAuditDataForEntity() {
    AuditEventsQuery query = new AuditEventsQuery(5, 1, null, null);
    when(repo.findCommitMetadataByEntity(
            anyString(), anyString(), any(), any(), anyInt(), anyInt()))
        .thenReturn(List.of(mock(CommitMetadata.class)));
    when(repo.countCommitMetadataByEntity(anyString(), anyString(), any(), any())).thenReturn(1L);
    AuditEventsResponse resp = service.fetchAuditDataForEntity("type", "1", query);
    assertEquals(1, resp.count());
    verify(repo)
        .findCommitMetadataByEntity(anyString(), anyString(), isNull(), isNull(), eq(5), eq(1));
  }
}
