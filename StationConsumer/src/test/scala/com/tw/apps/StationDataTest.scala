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
  }
}
