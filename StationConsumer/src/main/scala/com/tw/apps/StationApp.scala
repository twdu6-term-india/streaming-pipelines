package com.tw.apps

import StationDataTransformation._
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.functions.{col, current_date, date_format}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

object StationApp {

  def col(str: String): Column = ???

  def main(args: Array[String]): Unit = {

    val zookeeperConnectionString = if (args.isEmpty) "zookeeper:2181" else args(0)
    val retryPolicy = new ExponentialBackoffRetry(1000, 3)
    val zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy)

    zkClient.start()

    val stationKafkaBrokers = new String(zkClient.getData.forPath("/tw/stationStatus/kafkaBrokers"))
    val nycStationTopic = new String(zkClient.getData.watched.forPath("/tw/stationDataNYC/topic"))
    val sfStationTopic = new String(zkClient.getData.watched.forPath("/tw/stationDataSF/topic"))
    val marseilleStationTopic = new String(zkClient.getData.watched.forPath("/tw/stationDataMarseille/topic"))
    val checkpointLocation = new String(zkClient.getData.watched.forPath("/tw/output/checkpointLocation"))
    val outputLocation = new String(zkClient.getData.watched.forPath("/tw/output/dataLocation"))

    val errorCheckpointLocation = new String(zkClient.getData.watched.forPath("/tw/errorOutput/stationMart/checkpointLocation"))
    val errorLocation = new String(zkClient.getData.watched.forPath("/tw/errorOutput/stationMart/error"))

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    import spark.implicits._
    def createStreamTransformer(stationKafkaBrokers: String, topic: String, transformer: (DataFrame, SparkSession) => DataFrame) = {
      spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", stationKafkaBrokers)
        .option("subscribe", topic)
        .option("startingOffsets", "latest")
        .load()
        .selectExpr("CAST(value AS STRING) as raw_payload")
        .transform(transformer(_, spark))
    }

    val nycStationDF = createStreamTransformer(stationKafkaBrokers, nycStationTopic, nycStationStatusJson2DF)
    val sfStationDF = createStreamTransformer(stationKafkaBrokers, sfStationTopic, stationStatusJson2DF)
    val marseilleStationDF = createStreamTransformer(stationKafkaBrokers, marseilleStationTopic, stationStatusJson2DF)

    val aggregatedStreams = nycStationDF
      .union(sfStationDF)
      .union(marseilleStationDF)
      .as[StationData]

      aggregatedStreams
        .filter(x => x.isValid)
        .groupByKey(r => r.station_id)
        .reduceGroups((r1, r2) => if (r1.last_updated > r2.last_updated) r1 else r2)
        .map(_._2)
        .writeStream
        .format("overwriteCSV")
        .outputMode("complete")
        .option("header", true)
        .option("truncate", false)
        .option("checkpointLocation", checkpointLocation)
        .option("path", outputLocation)
        .start()

    aggregatedStreams
      .filter(x => !x.isValid)
      .map(x => x.getStationDataWithError)
      .withColumn("date", date_format(current_date(), "yyyy-MM-dd"))
      .writeStream
      .partitionBy("date")
      .outputMode("append")
      .option("header", true)
      .format("csv")
      .option("checkpointLocation", errorCheckpointLocation)
      .option("path", errorLocation)
      .start()

    spark.streams.awaitAnyTermination()

  }
}
