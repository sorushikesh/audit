package com.sorushi.invoice.management.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AuditApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuditApplication.class, args);
  }
}
