package com.github.mnogu.gatling.kafka.action

import com.github.mnogu.gatling.kafka.protocol.{KafkaComponents, KafkaProtocol}
import com.github.mnogu.gatling.kafka.request.builder.Avro4sAttributes
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer

import scala.collection.JavaConverters._

class KafkaAvro4sActionBuilder[K, V](avro4sAttributes: Avro4sAttributes[K, V]) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._

    val kafkaComponents: KafkaComponents = protocolComponentsRegistry.components(KafkaProtocol.KafkaProtocolKey)

    val producer = new KafkaProducer[K, GenericRecord](kafkaComponents.kafkaProtocol.properties.asJava)

    coreComponents.actorSystem.registerOnTermination(producer.close())

    new KafkaAvro4sRequestAction(
      producer,
      avro4sAttributes,
      coreComponents,
      kafkaComponents.kafkaProtocol,
      throttled,
      next
    )

  }

}
