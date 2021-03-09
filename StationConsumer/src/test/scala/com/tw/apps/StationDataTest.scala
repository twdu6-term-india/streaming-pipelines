package com.tw.apps

import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class StationDataTest extends FeatureSpec with Matchers with GivenWhenThen {
  feature("Create StationData & Invoke Validations") {
    scenario("StationData with valid inputs") {

      val stationData = StationData(
        bikes_available = Some(1),
        docks_available = Some(1),
        is_renting = true,
        is_returning = true,
        last_updated = 987654321,
        station_id = Some("abc"),
        name = Some("someone"),
        latitude = Some(1234567890),
        longitude = Some(1234567890)
      )

      stationData.isValid shouldBe true
    }

    scenario("StationData with invalid inputs") {

      val stationData = StationData(
        bikes_available = None,
        docks_available = Some(1),
        is_renting = true,
        is_returning = true,
        last_updated = 987654321,
        station_id = None,
        name = Some("someone"),
        latitude = Some(1234567890),
        longitude = Some(1234567890)
      )

      stationData.isValid shouldBe false
    }

    scenario("StationData with error column when having invalid input") {
      val stationData = StationData(
        bikes_available = None,
        docks_available = Some(1),
        is_renting = true,
        is_returning = true,
        last_updated = 987654321,
        station_id = Some("station 1"),
        name = Some("someone"),
        latitude = Some(1234567890),
        longitude = Some(1234567890)
      )

      val stationDataWithError = stationData.getStationDataWithError
      stationDataWithError.error should be("Invalid Bikes Available")
    }

    scenario("StationData with error column when having multiple invalid input") {
      val stationData = StationData(
        bikes_available = None,
        docks_available = Some(1),
        is_renting = true,
        is_returning = true,
        last_updated = 987654321,
        station_id = None,
        name = Some("someone"),
        latitude = None,
        longitude = Some(1234567890)
      )

      val stationDataWithError = stationData.getStationDataWithError
      val expectedErrorMessage =
        """Invalid Bikes Available  | Invalid Station Id  | Invalid Latitude""".stripMargin
      stationDataWithError.error should be(expectedErrorMessage)
    }
  }
}
