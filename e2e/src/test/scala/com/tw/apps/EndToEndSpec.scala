package com.tw.apps

import org.apache.spark.sql.SparkSession
import org.scalatest._

class EndToEndSpec extends FeatureSpec with Matchers with GivenWhenThen {

  feature("End to End spec") {
    val spark = SparkSession
      .builder
      .appName("E2e Test App")
      .master("local")
      .getOrCreate()
    import spark.implicits._

    ignore("should write correct station data to station mart") {
      val df2 = spark.read.csv("hdfs://localhost:9000/tw/stationMart/data")
      df2.show(10)
    }
  }
}
