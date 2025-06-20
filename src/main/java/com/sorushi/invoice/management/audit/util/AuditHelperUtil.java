package com.sorushi.invoice.management.audit.util;

import static com.sorushi.invoice.management.audit.exception.ErrorCodes.DATE_TIME_PARSING_ERROR;

import com.sorushi.invoice.management.audit.exception.AuditServiceException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditHelperUtil {

  private final MessageSource messageSource;

  public AuditHelperUtil(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public void validateDate(String date, List<DateTimeFormatter> allowedFormats) {

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
}
