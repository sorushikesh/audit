package com.sorushi.invoice.management.audit.repository;

import org.javers.core.commit.CommitMetadata;

public interface CommitMetadataRepository {
  void saveCommitMetadata(CommitMetadata commitMetadata);

  java.util.List<CommitMetadata> findAllCommitMetadata();

  java.util.List<CommitMetadata> findCommitMetadataByEntity(String entityType, String entityId);
}
