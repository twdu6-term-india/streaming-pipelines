package com.tw.apps

import java.time.Instant
import java.time.format.DateTimeFormatter
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{udf, _}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

import scala.util.parsing.json.JSON

object StationDataTransformation {


  def nycStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    import spark.implicits._

    jsonDF.select(from_json($"raw_payload", ScalaReflection.schemaFor[StationData].dataType) as "status")
      .select($"status.*")
  }

  def stationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val statusFunction: UserDefinedFunction = udf(stationStatus)

    import spark.implicits._
    jsonDF.select(explode(statusFunction(jsonDF("raw_payload"))) as "status")
      .select($"status.*")
  }

  val stationStatus: String => Seq[StationData] = raw_payload => {
    val json = JSON.parseFull(raw_payload)
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractStationStatus(payload)
  }

  private def extractStationStatus(payload: Any) = {

    val network: Any = payload.asInstanceOf[Map[String, Any]]("network")
    val stations: Any = network.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        StationData(
          extractIntValueSafelyTest(x, "free_bikes"),
          extractIntValueSafely(x, "empty_slots"),
          extractBooleanValueSafely(x("extra").asInstanceOf[Map[String, Any]], "renting"),
          extractBooleanValueSafely(x("extra").asInstanceOf[Map[String, Any]], "returning"),
          Instant.from(DateTimeFormatter.ISO_INSTANT.parse(x("timestamp").asInstanceOf[String])).getEpochSecond,
          extractStringValueSafely(x, "id"),
          extractStringValueSafely(x, "name"),
          extractDoubleValueSafely(x, "latitude"),
          extractDoubleValueSafely(x, "longitude")
        )
      })
  }

  private def extractStringValueSafely(data: Map[String, Any], key: String): Option[String] = {
    if (data.contains(key) && data(key).isInstanceOf[String])
      return Some(data(key).asInstanceOf[String])
    None
  }

  private def extractIntValueSafelyTest(data: Map[String, Any], key: String): Option[Integer] = {
    val dd = new scala.util.Random(1000)
    val value = dd.nextInt()
    if (value % 2 == 0)
      return Some(value)
    None
  }

  private def extractIntValueSafely(data: Map[String, Any], key: String): Option[Integer] = {
    if (data.contains(key) && data(key).isInstanceOf[Double])
      return Some(data(key).asInstanceOf[Double].toInt)
    None
  }

  private def extractDoubleValueSafely(data: Map[String, Any], key: String): Option[Double] = {
    if (data.contains(key) && data(key).isInstanceOf[Double])
      return Some(data(key).asInstanceOf[Double])
    None
  }

  private def extractBooleanValueSafely(data: Map[String, Any], key: String): Boolean = {
    if (data.contains(key))
      return data(key).asInstanceOf[Double] == 1

    true
  }

}
