package com.sorushi.invoice.management.audit.util;

import static com.sorushi.invoice.management.audit.constants.Constants.*;
import static com.sorushi.invoice.management.audit.exception.ErrorCodes.DATE_TIME_PARSING_ERROR;
import static com.sorushi.invoice.management.audit.exception.ErrorCodes.PARSE_FIELD_LIST_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditHelperUtil {

  private final MessageSource messageSource;
  ObjectMapper objectMapper = new ObjectMapper();

  public AuditHelperUtil(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public void validateDate(String date, List<DateTimeFormatter> allowedFormats)
      throws AuditServiceException {

    boolean parsedSuccessfully = false;
    for (DateTimeFormatter dateTimeFormatter : allowedFormats) {
      try {
        LocalDateTime.parse(date, dateTimeFormatter);
        parsedSuccessfully = true;
      } catch (DateTimeParseException dateTimeParseException) {
        log.error("Error occurred while parsing date {} to format {}.", date, dateTimeFormatter);
      }
    }

    if (!parsedSuccessfully) {
      throw new AuditServiceException(
          HttpStatus.BAD_REQUEST,
          DATE_TIME_PARSING_ERROR,
          new Object[] {date, allowedFormats},
          messageSource);
    }
  }

  public void filterRequestFields(AuditEvent auditEvent, List<String> fieldListInRequest)
      throws JsonProcessingException, AuditServiceException {

    List<String> fieldList = auditEvent.fieldList();
    Map<String, Object> newVal = auditEvent.newVal();

    if (fieldList == null || newVal == null) return;

    JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(newVal));

    for (String field : fieldList) {
      String path = SLASH + field.replace(DOT, SLASH);
      JsonNode value = jsonNode.at(path);

      if (value.isMissingNode()) {
        throw new AuditServiceException(
            HttpStatus.BAD_REQUEST, PARSE_FIELD_LIST_ERROR, null, messageSource);
      }

      fieldListInRequest.add(path);
    }
  }

  public void processFieldListIfPresentInRequest(
      AuditEvent auditEvent, List<String> fieldListInRequest) throws JsonProcessingException {

    if (auditEvent.fieldList() == null || auditEvent.fieldList().isEmpty()) return;

    JsonNode originalNode =
        objectMapper.readTree(objectMapper.writeValueAsString(auditEvent.newVal()));
    ObjectNode resultNode = objectMapper.createObjectNode();

    for (String fullPath : fieldListInRequest) {
      String[] parts = fullPath.split(SLASH);
      if (parts.length < 2) continue;

      JsonNode value = originalNode.at(fullPath);
      if (value.isMissingNode()) continue;

      ObjectNode current = resultNode;
      for (int i = 1; i < parts.length - 1; i++) {
        String key = parts[i];
        JsonNode child = current.get(key);
        if (child == null || !child.isObject()) {
          ObjectNode newChild = objectMapper.createObjectNode();
          current.set(key, newChild);
          current = newChild;
        } else {
          current = (ObjectNode) child;
        }
      }
      current.set(parts[parts.length - 1], value);
    }
  }

  public Map<String, String> addCommitPropertiesMap(AuditEvent auditEvent) {
    JaversUtil.clearPropertiesMap();
    JaversUtil.addJaversCommitProperties(TYPE, auditEvent.entityType());
    JaversUtil.addJaversCommitProperties(TYPE_ID, auditEvent.entityId());
    JaversUtil.addJaversCommitProperties(OPERATION, auditEvent.operation());
    JaversUtil.addJaversCommitProperties(USER_ID, auditEvent.author());

    if (StringUtils.isNotEmpty(auditEvent.changedDate())) {
      JaversUtil.addJaversCommitProperties(CHANGED_DATE, auditEvent.changedDate());
    }

    return JaversUtil.getJaversCommitPropertiesMap();
  }
}
