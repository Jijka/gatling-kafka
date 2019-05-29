package com.github.mnogu.gatling.kafka.action

import com.github.mnogu.gatling.kafka.protocol.KafkaProtocol
import com.github.mnogu.gatling.kafka.request.builder.Avro4sAttributes
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.DefaultClock
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session._
import io.gatling.core.util.NameGen
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer._

class KafkaAvro4sRequestAction[K, V](val producer: KafkaProducer[K, GenericRecord],
                                     val avro4sAttributes: Avro4sAttributes[K, V],
                                     val coreComponents: CoreComponents,
                                     val kafkaProtocol: KafkaProtocol,
                                     val throttled: Boolean,
                                     val next: Action)
  extends ExitableAction with NameGen {

  val statsEngine   = coreComponents.statsEngine
  val clock         = new DefaultClock
  override val name = genName("kafkaAvroRequest")

  override def execute(session: Session): Unit = recover(session) {
    avro4sAttributes requestName session flatMap { requestName =>
      val outcome = sendRequest(requestName, producer, avro4sAttributes, throttled, session)

      outcome.onFailure(
        errorMessage => statsEngine.reportUnbuildableRequest(session, requestName, errorMessage)
      )

      outcome
    }
  }

  def sendRequest(requestName: String,
                  producer: KafkaProducer[K, GenericRecord],
                  avro4sAttributes: Avro4sAttributes[K, V],
                  throttled: Boolean,
                  session: Session): Validation[Unit] = {

    avro4sAttributes payload session map { payload =>
      val record = avro4sAttributes.key
        .map(k => new ProducerRecord(kafkaProtocol.topic, k(session).toOption.get, avro4sAttributes.format.to(payload)))
        .getOrElse(new ProducerRecord(kafkaProtocol.topic, avro4sAttributes.format.to(payload)))

      val requestStartDate = clock.nowMillis

      producer.send(
        record,
        (_: RecordMetadata, e: Exception) => {

          val requestEndDate = clock.nowMillis
          statsEngine.logResponse(
            session,
            requestName,
            requestStartDate,
            requestEndDate,
            if (e == null) OK else KO,
            None,
            if (e == null) None else Some(e.getMessage)
          )

          if (throttled) {
            coreComponents.throttler.throttle(session.scenario, () => next ! session)
          } else {
            next ! session
          }

        }
      )

    }

  }

}
