package com.sorushi.invoice.management.audit;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Base class that starts a lightweight container for all tests. */
@Testcontainers
public abstract class BaseContainerTest {

  @Container
  protected static final GenericContainer<?> ALPINE =
      new GenericContainer<>(DockerImageName.parse("alpine:3.19"))
          .withCommand("sleep", "1");
}
