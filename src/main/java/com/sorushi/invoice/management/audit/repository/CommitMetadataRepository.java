package com.sorushi.invoice.management.audit.repository;

import org.javers.core.commit.CommitMetadata;

public interface CommitMetadataRepository {
  void saveCommitMetadata(CommitMetadata commitMetadata);
}
