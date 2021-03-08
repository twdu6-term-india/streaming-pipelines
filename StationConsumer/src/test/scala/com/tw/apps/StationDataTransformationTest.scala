package com.tw.apps

import StationDataTransformation.{nycStationStatusJson2DF, stationStatusJson2DF}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.ScalaReflection
import org.scalatest._

import scala.None

class StationDataTransformationTest extends FeatureSpec with Matchers with GivenWhenThen {

  feature("Apply station status transformations to data frame") {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
    import spark.implicits._

    scenario("Transform nyc station data frame") {

      val testStationData =
        """{
          "station_id":"83",
          "bikes_available":19,
          "docks_available":41,
          "is_renting":true,
          "is_returning":true,
          "last_updated":1536242527,
          "name":"Atlantic Ave & Fort Greene Pl",
          "latitude":40.68382604,
          "longitude":-73.97632328
          }"""

      val schema = ScalaReflection.schemaFor[StationData].dataType //.asInstanceOf[StructType]

      Given("Sample data for station_status")
      val testDF1 = Seq(testStationData).toDF("raw_payload")


      When("Transformations are applied")
      val resultDF1 = testDF1.transform(nycStationStatusJson2DF(_, spark))

      Then("Useful columns are extracted")
      resultDF1.schema.fields(0).name should be("bikes_available")
      resultDF1.schema.fields(0).dataType.typeName should be("integer")
      resultDF1.schema.fields(1).name should be("docks_available")
      resultDF1.schema.fields(1).dataType.typeName should be("integer")
      resultDF1.schema.fields(2).name should be("is_renting")
      resultDF1.schema.fields(2).dataType.typeName should be("boolean")
      resultDF1.schema.fields(3).name should be("is_returning")
      resultDF1.schema.fields(3).dataType.typeName should be("boolean")
      resultDF1.schema.fields(4).name should be("last_updated")
      resultDF1.schema.fields(4).dataType.typeName should be("long")
      resultDF1.schema.fields(5).name should be("station_id")
      resultDF1.schema.fields(5).dataType.typeName should be("string")
      resultDF1.schema.fields(6).name should be("name")
      resultDF1.schema.fields(6).dataType.typeName should be("string")
      resultDF1.schema.fields(7).name should be("latitude")
      resultDF1.schema.fields(7).dataType.typeName should be("double")
      resultDF1.schema.fields(8).name should be("longitude")
      resultDF1.schema.fields(8).dataType.typeName should be("double")

      val row1 = resultDF1.head()
      row1.get(0) should be(19)
      row1.get(1) should be(41)
      row1.get(2) shouldBe true
      row1.get(3) shouldBe true
      row1.get(4) should be(1536242527)
      row1.get(5) should be("83")
      row1.get(6) should be("Atlantic Ave & Fort Greene Pl")
      row1.get(7) should be(40.68382604)
      row1.get(8) should be(-73.97632328)
    }

    scenario("Transform marseille data to DF") {
      val testMarseilleStationData =
        """{
             "payload":{
                "network":{
                   "company":[
                      "JCDecaux"
                   ],
                   "href":"/v2/networks/le-velo",
                   "id":"abcs12312",
                   "license":{
                      "name":"Open Licence",
                      "url":"https://developer.jcdecaux.com/#/opendata/licence"
                   },
                   "location":{
                      "city":"Marseille",
                      "country":"FR",
                      "latitude":43.296482,
                      "longitude":5.36978
                   },
                   "name":"Le v\u00e9lo",
                   "source":"https://developer.jcdecaux.com",
                   "stations":[
                      {
                         "empty_slots":5,
                         "free_bikes":10,
                         "id":"abcs12312",
                         "latitude":43.31181964585524,
                         "longitude":5.387375678428276,
                         "name":"3320 - BELLE DE MAI CADENAT",
                         "timestamp":"2021-02-25T08:39:18Z",
                         "extra":{
                            "address":"BELLE DE MAI CADENAT - PLACE BERNARD CADENAT",
                            "banking":true,
                            "bonus":false,
                            "last_update":1614242296000,
                            "slots":15,
                            "status":"OPEN",
                            "uid":3320
                         }
                      }
                   ]
                }
             }
          }"""


      Given("Sample data for station status")
      val testDF1 = Seq(testMarseilleStationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF1 = testDF1.transform(stationStatusJson2DF(_, spark))

      Then("Useful columns are extracted")
      resultDF1.schema.fields(0).name should be("bikes_available")
      resultDF1.schema.fields(0).dataType.typeName should be("integer")
      resultDF1.schema.fields(1).name should be("docks_available")
      resultDF1.schema.fields(1).dataType.typeName should be("integer")
      resultDF1.schema.fields(2).name should be("is_renting")
      resultDF1.schema.fields(2).dataType.typeName should be("boolean")
      resultDF1.schema.fields(3).name should be("is_returning")
      resultDF1.schema.fields(3).dataType.typeName should be("boolean")
      resultDF1.schema.fields(4).name should be("last_updated")
      resultDF1.schema.fields(4).dataType.typeName should be("long")
      resultDF1.schema.fields(5).name should be("station_id")
      resultDF1.schema.fields(5).dataType.typeName should be("string")
      resultDF1.schema.fields(6).name should be("name")
      resultDF1.schema.fields(6).dataType.typeName should be("string")
      resultDF1.schema.fields(7).name should be("latitude")
      resultDF1.schema.fields(7).dataType.typeName should be("double")
      resultDF1.schema.fields(8).name should be("longitude")
      resultDF1.schema.fields(8).dataType.typeName should be("double")

      val row1 = resultDF1.head()
      row1.get(0) should be(10)
      row1.get(1) should be(5)
      row1.get(2) shouldBe true
      row1.get(3) shouldBe true
      row1.get(4) should be(1614242358)
      row1.get(5) should be("abcs12312")
      row1.get(6) should be("3320 - BELLE DE MAI CADENAT")
      row1.get(7) should be(43.31181964585524)
      row1.get(8) should be(5.387375678428276)

    }

    scenario("Transform SF data to DF") {
      val testMarseilleStationData =
        """{
             "payload":{
                "network":{
                   "company":[
                      "JCDecaux"
                   ],
                   "href":"/v2/networks/le-velo",
                   "id":"abcs12312",
                   "license":{
                      "name":"Open Licence",
                      "url":"https://developer.jcdecaux.com/#/opendata/licence"
                   },
                   "location":{
                      "city":"Marseille",
                      "country":"FR",
                      "latitude":43.296482,
                      "longitude":5.36978
                   },
                   "name":"Le v\u00e9lo",
                   "source":"https://developer.jcdecaux.com",
                   "stations":[
                      {
                         "empty_slots":5,
                         "free_bikes":10,
                         "id":"abcs12312",
                         "latitude":43.31181964585524,
                         "longitude":5.387375678428276,
                         "name":"3320 - BELLE DE MAI CADENAT",
                         "timestamp":"2021-02-25T08:39:18Z",
                         "extra":{
                            "address":"BELLE DE MAI CADENAT - PLACE BERNARD CADENAT",
                            "renting":1,
                            "returning":0,
                            "last_update":1614242296000,
                            "slots":15,
                            "status":"OPEN",
                            "uid":3320
                         }
                      }
                   ]
                }
             }
          }"""


      Given("Sample data for station status")
      val testDF1 = Seq(testMarseilleStationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF1 = testDF1.transform(stationStatusJson2DF(_, spark))

      Then("Useful columns are extracted")
      resultDF1.schema.fields(0).name should be("bikes_available")
      resultDF1.schema.fields(0).dataType.typeName should be("integer")
      resultDF1.schema.fields(1).name should be("docks_available")
      resultDF1.schema.fields(1).dataType.typeName should be("integer")
      resultDF1.schema.fields(2).name should be("is_renting")
      resultDF1.schema.fields(2).dataType.typeName should be("boolean")
      resultDF1.schema.fields(3).name should be("is_returning")
      resultDF1.schema.fields(3).dataType.typeName should be("boolean")
      resultDF1.schema.fields(4).name should be("last_updated")
      resultDF1.schema.fields(4).dataType.typeName should be("long")
      resultDF1.schema.fields(5).name should be("station_id")
      resultDF1.schema.fields(5).dataType.typeName should be("string")
      resultDF1.schema.fields(6).name should be("name")
      resultDF1.schema.fields(6).dataType.typeName should be("string")
      resultDF1.schema.fields(7).name should be("latitude")
      resultDF1.schema.fields(7).dataType.typeName should be("double")
      resultDF1.schema.fields(8).name should be("longitude")
      resultDF1.schema.fields(8).dataType.typeName should be("double")

      val row1 = resultDF1.head()
      row1.get(0) should be(10)
      row1.get(1) should be(5)
      row1.get(2) shouldBe true
      row1.get(3) shouldBe false
      row1.get(4) should be(1614242358)
      row1.get(5) should be("abcs12312")
      row1.get(6) should be("3320 - BELLE DE MAI CADENAT")
      row1.get(7) should be(43.31181964585524)
      row1.get(8) should be(5.387375678428276)

    }

  }
}
