package com.sorushi.invoice.management.audit.service.serviceImpl;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.service.AuditService;
import com.sorushi.invoice.management.audit.util.AuditHelperUtil;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

  private final AuditHelperUtil auditHelperUtil;

  public AuditServiceImpl(AuditHelperUtil auditHelperUtil) {
    this.auditHelperUtil = auditHelperUtil;
  }

  @Override
  public void processAuditEvent(AuditEvent auditEvent) {

    log.info("Validating date {}", auditEvent.changedDate());
    auditHelperUtil.validateDate(
        auditEvent.changedDate(), Collections.singletonList(DateTimeFormatter.ISO_DATE_TIME));
  }
}
