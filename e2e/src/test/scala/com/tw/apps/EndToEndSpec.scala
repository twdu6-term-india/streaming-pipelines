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

    scenario("should write correct station data to station mart") {

      Given("station mart contains output file")
      val df2 = spark.read
        .option("header", true)
        .csv("hdfs://hadoop:9000/tw/stationMart/data")


      Then("csv file in station mart should contain valid station data")

      df2.count() should be (4)

    }
  }
}
