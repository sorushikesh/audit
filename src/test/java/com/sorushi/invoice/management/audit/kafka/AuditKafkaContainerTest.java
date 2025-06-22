package com.sorushi.invoice.management.audit.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
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
    Map<String, Object> props =
        new HashMap<>(KafkaTestUtils.producerProps(KAFKA.getBootstrapServers()));
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    ProducerFactory<String, AuditEvent> pf = new DefaultKafkaProducerFactory<>(props);
    template = new KafkaTemplate<>(pf);
    template.setDefaultTopic("audit-log");
  }

  static final String TOPIC_PRODUCER = "audit-log-producer";
  static final String TOPIC_LISTENER = "audit-log-listener";

  @Test
  void producerSendsAndConsumerReceives() {
    AuditEventProducer producer = new AuditEventProducer(template);
    ReflectionTestUtils.setField(producer, "auditTopic", TOPIC_PRODUCER);

    Map<String, Object> consumerProps =
        new HashMap<>(KafkaTestUtils.consumerProps(KAFKA.getBootstrapServers(), "grp1", "true"));
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AuditEvent.class.getName());
    ConsumerFactory<String, AuditEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
    Consumer<String, AuditEvent> consumer = cf.createConsumer();
    consumer.subscribe(List.of(TOPIC_PRODUCER));

    AuditEvent event = new AuditEvent("1", "t", "1", null, null, null, Map.of(), null);
    producer.sendAuditEvent(event);

    ConsumerRecord<String, AuditEvent> record =
        KafkaTestUtils.getSingleRecord(consumer, TOPIC_PRODUCER);
    assertEquals(event.id(), record.value().id());
    consumer.close();
  }

  @Test
  void listenerProcessesEventFromKafka() throws JsonProcessingException {
    AuditService service = mock(AuditService.class);
    AuditKafkaListener listener = new AuditKafkaListener(service);

    AuditEventProducer producer = new AuditEventProducer(template);
    ReflectionTestUtils.setField(producer, "auditTopic", TOPIC_LISTENER);
    AuditEvent event = new AuditEvent("2", "t", "2", null, null, null, Map.of(), null);
    producer.sendAuditEvent(event);

    Map<String, Object> consumerProps =
        new HashMap<>(KafkaTestUtils.consumerProps(KAFKA.getBootstrapServers(), "grp2", "true"));
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AuditEvent.class.getName());
    ConsumerFactory<String, AuditEvent> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
    Consumer<String, AuditEvent> consumer = cf.createConsumer();
    consumer.subscribe(List.of(TOPIC_LISTENER));
    ConsumerRecord<String, AuditEvent> record =
        KafkaTestUtils.getSingleRecord(consumer, TOPIC_LISTENER);
    listener.listen(record.value());
    verify(service).processAuditEvent(event);
    consumer.close();
  }
}
