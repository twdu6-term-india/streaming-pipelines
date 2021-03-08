package com.tw.apps

case class StationData(
                        bikes_available: Option[Integer],
                        docks_available: Option[Integer],
                        is_renting: Boolean,
                        is_returning: Boolean,
                        last_updated: Long,
                        station_id: Option[String],
                        name: Option[String],
                        latitude: Option[Double],
                        longitude: Option[Double]
                      ) {

  def isValid: Boolean = {
    (!bikes_available.isEmpty && bikes_available.get > 0) &&
      (!docks_available.isEmpty && docks_available.get > 0) &&
      !latitude.isEmpty &&
      !longitude.isEmpty &&
      !station_id.isEmpty &&
      !name.isEmpty
  }
}
