package com.sorushi.invoice.management.audit.service.serviceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.BaseContainerTest;
import com.sorushi.invoice.management.audit.dto.AuditEventsQuery;
import com.sorushi.invoice.management.audit.dto.AuditEventsResponse;
import com.sorushi.invoice.management.audit.dto.EntityHistoryResponse;
import com.sorushi.invoice.management.audit.model.AuditEventJavers;
import com.sorushi.invoice.management.audit.constants.Constants;
import com.sorushi.invoice.management.audit.repository.repositoryImpl.CommitMetadataRepositoryImpl;
import com.sorushi.invoice.management.audit.util.AuditHelperUtil;
import com.sorushi.invoice.management.audit.util.JaversUtil;
import java.util.Collections;
import java.util.List;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.commit.CommitId;
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
  void fetchAuditDataParsesOffsetDates() {
    AuditEventsQuery query =
        new AuditEventsQuery(1, 0, "2024-01-01T00:00:00Z", "2024-01-02T00:00:00Z");
    when(repo.findCommitMetadata(any(), any(), anyInt(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(repo.countCommitMetadata(any(), any())).thenReturn(0L);

    AuditEventsResponse resp = service.fetchAuditData(query);
    assertEquals(0, resp.count());
    verify(repo).findCommitMetadata(any(), any(), eq(1), eq(0));
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

  @Test
  void fetchAuditDataForUser() {
    AuditEventsQuery query = new AuditEventsQuery(2, 0, null, null);
    when(repo.findCommitMetadataByUser(anyString(), any(), any(), anyInt(), anyInt()))
        .thenReturn(List.of(mock(CommitMetadata.class)));
    when(repo.countCommitMetadataByUser(anyString(), any(), any())).thenReturn(1L);

    AuditEventsResponse resp = service.fetchAuditDataForUser("user", query);

    assertEquals(1, resp.count());
    verify(repo)
        .findCommitMetadataByUser(eq("user"), isNull(), isNull(), eq(2), eq(0));
  }

  @Test
  void fetchEntityHistory() {
    CommitMetadata cm1 = mock(CommitMetadata.class);
    CommitMetadata cm2 = mock(CommitMetadata.class);
    CommitId id1 = mock(CommitId.class);
    CommitId id2 = mock(CommitId.class);
    when(id1.getMajorId()).thenReturn(1L);
    when(id2.getMajorId()).thenReturn(2L);
    when(cm1.getId()).thenReturn(id1);
    when(cm2.getId()).thenReturn(id2);
    when(cm1.getCommitDate()).thenReturn(new java.util.Date(1));
    when(cm2.getCommitDate()).thenReturn(new java.util.Date(2));
    when(cm1.getAuthor()).thenReturn("a1");
    when(cm2.getAuthor()).thenReturn("a2");
    when(cm1.getProperties()).thenReturn(java.util.Map.of(Constants.OPERATION, "Create"));
    when(cm2.getProperties()).thenReturn(java.util.Map.of(Constants.OPERATION, "Update"));

    when(repo.findCommitMetadataByEntity(anyString(), anyString(), any(), any(), anyInt(), anyInt()))
        .thenReturn(List.of(cm1, cm2));

    AuditEventJavers j1 = new AuditEventJavers();
    AuditEventJavers j2 = new AuditEventJavers();
    when(javers.getLastUpdatedVersion("type", "1", 1L, 0)).thenReturn(j1);
    when(javers.getLastUpdatedVersion("type", "1", 2L, 0)).thenReturn(j2);

    EntityHistoryResponse resp = service.fetchEntityHistory("type", "1");
    assertEquals(2, resp.count());
    verify(repo)
        .findCommitMetadataByEntity(eq("type"), eq("1"), isNull(), isNull(), eq(0), eq(0));
  }
}
