package com.sorushi.invoice.management.audit.repository;

import java.util.Date;
import java.util.List;
import org.javers.core.commit.CommitMetadata;

public interface CommitMetadataRepository {
  void saveCommitMetadata(CommitMetadata commitMetadata);

  List<CommitMetadata> findCommitMetadata(Date startDate, Date endDate, int limit, int skip);

  long countCommitMetadata(Date startDate, Date endDate);

  List<CommitMetadata> findCommitMetadataByEntity(
      String entityType, String entityId, Date startDate, Date endDate, int limit, int skip);

  long countCommitMetadataByEntity(
      String entityType, String entityId, Date startDate, Date endDate);

  List<CommitMetadata> findCommitMetadataByUser(
      String userId, Date startDate, Date endDate, int limit, int skip);

  long countCommitMetadataByUser(String userId, Date startDate, Date endDate);
}
