package com.tw.apps

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.scalatest._

class EndToEndSpec extends FeatureSpec with Matchers with GivenWhenThen {

  feature("End to End spec") {
    val spark = SparkSession
      .builder
      .appName("E2e Test App")
      .master("local")
      .getOrCreate()

    val tempStationMartLocation = "/user/hadoop/tempTwStationMart"

    scenario("should write correct station data to station mart") {

      val conf = new org.apache.hadoop.conf.Configuration()
      val srcPath = new org.apache.hadoop.fs.Path("hdfs://hadoop:9000/tw/stationMart/data")
      val dstPath = new org.apache.hadoop.fs.Path("hdfs://hadoop:9000" + tempStationMartLocation)

      org.apache.hadoop.fs.FileUtil.copy(
        srcPath.getFileSystem(conf),
        srcPath,
        dstPath.getFileSystem(conf),
        dstPath,
        true,
        conf
      )

      val actualDF = spark.read
        .format("csv")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("hdfs://hadoop:9000" + tempStationMartLocation)

      import spark.implicits._

      val expectedDF = Seq(
        (11, 4, true, true, 1602190953, "sf-station-1", "Harmon St at Adeline St", 37.849735, -122.270582),
        (8, 3, true, true, 1602190953, "sf-station-2", "Fountain Alley at S 2nd St", 37.33618830029063, -121.88927650451659),
        (6, 27, true, true, 1614256515, "79", "Franklin St & W Broadway", 40.71911552, -74.00666661),
        (19, 34, true, true, 1614256515, "72", "W 52 St & 11 Ave", 40.76727216, -73.99392888),
        (7, 2, true, true, 1614257973, "france-station-2", "9207- TEISSEIRE - ROUBAUD", 43.27252367086013, 5.399686062414487),
        (10, 9, true, true, 1614257973, "france-station-1", "8149-391 MICHELET", 43.25402727813068, 5.401873594694653))
        .toDF("bikes_available", "docks_available", "is_renting", "is_returning",
          "last_updated", "station_id", "name", "latitude", "longitude")

      Then("csv file in station mart should contain valid station data")

      actualDF.collect() should contain theSameElementsAs (expectedDF.collect())
      actualDF.columns should contain theSameElementsAs (expectedDF.columns)

    }

    scenario("should write incorrect station data to error path") {

      val actualErrorDF = spark.read
        .format("csv")
        .option("header", "true")
        .option("inferSchema", "true")
        .load("hdfs://hadoop:9000/tw/error/stationMart/data").dropDuplicates("station_id")


      import spark.implicits._

      val expectedErrorDF = Seq((null, 2, true, true, 1614257973, "france-station-2", "9207- TEISSEIRE - ROUBAUD",
        43.27252367086013, 5.399686062414487, "Invalid Bikes Available", "2021-03-09"))
        .toDF("bikes_available", "docks_available", "is_renting", "is_returning", "last_updated", "station_id",
          "name", "latitude", "longitude", "error", "date")
        .withColumn("date", col("date").cast("date"))


      Then("csv file in error path should contain only the invalid records")

      actualErrorDF.collect() should contain theSameElementsAs (expectedErrorDF.collect())
      actualErrorDF.columns should contain theSameElementsAs (expectedErrorDF.columns)


    }

  }
}
