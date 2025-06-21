package com.sorushi.invoice.management.audit.repository.repositoryImpl;

import static com.sorushi.invoice.management.audit.constants.Constants.COLLECTION_COMMIT_METADATA_COLLECTION;

import com.sorushi.invoice.management.audit.repository.CommitMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.javers.core.commit.CommitMetadata;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class CommitMetadataRepositoryImpl implements CommitMetadataRepository {

  private final MongoTemplate mongoTemplate;

  public CommitMetadataRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public void saveCommitMetadata(CommitMetadata commitMetadata) {
    log.info(
        "Saving commit metadata: {} in collection: {}",
        commitMetadata,
        COLLECTION_COMMIT_METADATA_COLLECTION);
    try {
      CommitMetadata savedCommit =
          mongoTemplate.save(commitMetadata, COLLECTION_COMMIT_METADATA_COLLECTION);
      log.debug("Successfully saved commit metadata with ID: {}", savedCommit.getId());
    } catch (Exception e) {
      log.error("Failed to save commit metadata: {}", commitMetadata, e);
      throw e;
    }
  }

  @Override
  public java.util.List<CommitMetadata> findAllCommitMetadata() {
    return mongoTemplate.findAll(CommitMetadata.class, COLLECTION_COMMIT_METADATA_COLLECTION);
  }

  @Override
  public java.util.List<CommitMetadata> findCommitMetadataByEntity(String entityType, String entityId) {
    org.springframework.data.mongodb.core.query.Query query =
        new org.springframework.data.mongodb.core.query.Query(
            org.springframework.data.mongodb.core.query.Criteria.where("properties.type")
                .is(entityType)
                .and("properties.typeId")
                .is(entityId));
    return mongoTemplate.find(query, CommitMetadata.class, COLLECTION_COMMIT_METADATA_COLLECTION);
  }
}
