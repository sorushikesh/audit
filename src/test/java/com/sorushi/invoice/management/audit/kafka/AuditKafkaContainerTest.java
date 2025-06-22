package com.sorushi.invoice.management.audit.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sorushi.invoice.management.audit.dto.AuditEvent;
import com.sorushi.invoice.management.audit.kafka.listener.AuditKafkaListener;
import com.sorushi.invoice.management.audit.kafka.producer.AuditEventProducer;
import com.sorushi.invoice.management.audit.service.AuditService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class AuditKafkaContainerTest {

  @Container
  static final KafkaContainer KAFKA =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

  private KafkaTemplate<String, AuditEvent> template;

  @BeforeEach
  void setup() {
    Map<String, Object> props = new HashMap<>(KafkaTestUtils.producerProps(KAFKA.getBootstrapServers()));
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    ProducerFactory<String, AuditEvent> pf = new DefaultKafkaProducerFactory<>(props);
    template = new KafkaTemplate<>(pf);
    template.setDefaultTopic("audit-log");
  }

  @Test
  void producerSendsAndConsumerReceives() {
    AuditEventProducer producer = new AuditEventProducer(template);
    ReflectionTestUtils.setField(producer, "auditTopic", "audit-log");

    Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("grp1", "true", KAFKA));
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AuditEvent.class.getName());
    ConsumerFactory<String, AuditEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
    Consumer<String, AuditEvent> consumer = cf.createConsumer();
    consumer.subscribe(List.of("audit-log"));

    AuditEvent event = new AuditEvent("1", "t", "1", null, null, null, Map.of(), null);
    producer.sendAuditEvent(event);

    ConsumerRecord<String, AuditEvent> record =
        KafkaTestUtils.getSingleRecord(consumer, "audit-log", 10000L);
    assertEquals(event.id(), record.value().id());
    consumer.close();
  }

  @Test
  void listenerProcessesEventFromKafka() {
    AuditService service = mock(AuditService.class);
    AuditKafkaListener listener = new AuditKafkaListener(service);

    AuditEventProducer producer = new AuditEventProducer(template);
    ReflectionTestUtils.setField(producer, "auditTopic", "audit-log");
    AuditEvent event = new AuditEvent("2", "t", "2", null, null, null, Map.of(), null);
    producer.sendAuditEvent(event);

    Map<String, Object> consumerProps = new HashMap<>(KafkaTestUtils.consumerProps("grp2", "true", KAFKA));
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AuditEvent.class.getName());
    ConsumerFactory<String, AuditEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
    Consumer<String, AuditEvent> consumer = cf.createConsumer();
    consumer.subscribe(List.of("audit-log"));
    ConsumerRecord<String, AuditEvent> record =
        KafkaTestUtils.getSingleRecord(consumer, "audit-log", 10000L);
    listener.listen(record.value());
    verify(service).processAuditEvent(event);
    consumer.close();
  }
}

