package com.github.mnogu.gatling.kafka

import java.time.Instant

import com.sksamuel.avro4s.{FromValue, ToSchema, ToValue}
import org.apache.avro.Schema.Field
import org.apache.avro.{LogicalTypes, Schema}

object AvroFormats {

  implicit object InstantToSchema extends ToSchema[Instant] {
    override val schema: Schema = LogicalTypes.timestampMillis.addToSchema(Schema.create(Schema.Type.LONG))
  }

  implicit object InstantToValue extends ToValue[Instant] {
    override def apply(value: Instant): Long = value.toEpochMilli
  }

  implicit object InstantFromValue extends FromValue[Instant] {
    override def apply(value: Any, field: Field): Instant = Instant.ofEpochMilli(value.asInstanceOf[Long])
  }

}
