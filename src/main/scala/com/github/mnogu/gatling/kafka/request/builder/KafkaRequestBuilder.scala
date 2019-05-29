package com.github.mnogu.gatling.kafka.request.builder

import com.github.mnogu.gatling.kafka.action.{KafkaAvro4sActionBuilder, KafkaRequestActionBuilder}
import com.sksamuel.avro4s.{FromRecord, RecordFormat, SchemaFor}
import io.gatling.core.session._

case class KafkaAttributes[K, V](requestName: Expression[String], key: Option[Expression[K]], payload: Expression[V])

case class Avro4sAttributes[K, V](requestName: Expression[String],
                                  key: Option[Expression[K]],
                                  schema: SchemaFor[V],
                                  format: RecordFormat[V],
                                  fromRecord: FromRecord[V],
                                  payload: Expression[V])

case class KafkaRequestBuilder(requestName: Expression[String]) {

  def send[V](payload: Expression[V]): KafkaRequestActionBuilder[_, V] = send(payload, None)

  def send[K, V](key: Expression[K], payload: Expression[V]): KafkaRequestActionBuilder[K, V] = send(payload, Some(key))

  private def send[K, V](payload: Expression[V], key: Option[Expression[K]]) =
    new KafkaRequestActionBuilder(KafkaAttributes(requestName, key, payload))

  def sendAvro[K, V](key: Expression[K], payload: Expression[V])(implicit schema: SchemaFor[V],
                                                                 format: RecordFormat[V],
                                                                 fromRecord: FromRecord[V]): KafkaAvro4sActionBuilder[K, V] =
    sendAvro(payload, Some(key))

  def sendAvro[V](payload: Expression[V])(implicit schema: SchemaFor[V],
                                          format: RecordFormat[V],
                                          fromRecord: FromRecord[V]): KafkaAvro4sActionBuilder[_, V] =
    sendAvro(payload, None)

  private def sendAvro[K, V](payload: Expression[V], key: Option[Expression[K]])(
    implicit schema: SchemaFor[V],
    format: RecordFormat[V],
    fromRecord: FromRecord[V]): KafkaAvro4sActionBuilder[K, V] =
    new KafkaAvro4sActionBuilder(Avro4sAttributes(requestName, None, schema, format, fromRecord, payload))

}
