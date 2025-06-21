package com.sorushi.invoice.management.audit.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.javers.core.metamodel.annotation.Entity;
import org.javers.core.metamodel.annotation.Id;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class AuditEventJavers {

  @Id private String id;
  private JsonNode jsonNode;
}
