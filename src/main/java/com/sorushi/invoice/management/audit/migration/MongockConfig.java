package com.sorushi.invoice.management.audit.migration;

import io.mongock.driver.mongodb.springdata.v4.driver.SpringDataMongoV4Driver;
import io.mongock.runner.springboot.base.MongockApplicationRunner;
import io.mongock.runner.standalone.MongockStandalone;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongockConfig {

  @Bean
  public MongockApplicationRunner mongockApplicationRunner(
      ApplicationContext springContext, MongoTemplate mongoTemplate) {
    var driver = SpringDataMongoV4Driver.withDefaultLock(mongoTemplate);
    return MongockStandalone.builder()
        .setDriver(driver)
        .addMigrationScanPackage("com.sorushi.invoice.management.audit.migration")
        .setSpringContext(springContext)
        .buildApplicationRunner();
  }
}
