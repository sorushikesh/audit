package com.sorushi.invoice.management.audit.migration;

import io.mongock.runner.springboot.base.MongockApplicationRunner;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class MongockTrigger {

  private final MongockApplicationRunner mongockApplicationRunner;

  public MongockTrigger(MongockApplicationRunner mongockApplicationRunner) {
    this.mongockApplicationRunner = mongockApplicationRunner;
  }

  @PostConstruct
  public void runMigrations() {
    try {
      ApplicationArguments args = new DefaultApplicationArguments();
      mongockApplicationRunner.run(args);
    } catch (Exception e) {
      throw new RuntimeException("Failed to run Mongock migration", e);
    }
  }
}
