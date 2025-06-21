package com.sorushi.invoice.management.audit.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JaversTTLConfig {

  @Value("${javers.ttl.commit-metadata-days:30}")
  private int commitMetadataDays;

  @Value("${javers.ttl.snapshot-days:30}")
  private int snapshotDays;
}

