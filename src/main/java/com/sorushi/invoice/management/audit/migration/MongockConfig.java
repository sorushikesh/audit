package com.sorushi.invoice.management.audit.migration;

import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver;
import io.mongock.runner.springboot.MongockSpringboot;
import io.mongock.runner.springboot.base.MongockApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@SuppressWarnings("unused")
@Configuration
public class MongockConfig {

  @Value("${mongock.change-log-package}")
  private String migrationPackage;

  @Bean
  public MongockApplicationRunner mongockApplicationRunner(
      ApplicationContext springContext, MongoTemplate mongoTemplate) {
    var driver = SpringDataMongoV4Driver.withDefaultLock(mongoTemplate);
    return MongockSpringboot.builder()
        .setDriver(driver)
        .addMigrationScanPackage(migrationPackage)
        .setSpringContext(springContext)
        .buildApplicationRunner();
  }
}
