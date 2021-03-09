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

  def getStationDataWithError: StationDataWithError = {
    StationDataWithError(
      this.bikes_available,
      this.docks_available,
      this.is_renting,
      this.is_returning,
      this.last_updated,
      this.station_id,
      this.name,
      this.latitude,
      this.longitude,
      constructErrorMessage)
  }

  private def constructErrorMessage: String = {
    var error = ""

    if (bikes_available.isEmpty || bikes_available.get < 0) error += "Invalid Bikes Available"
    if (docks_available.isEmpty || docks_available.get < 0) error += "  | Invalid Docks Available"
    if (station_id.isEmpty) error += "  | Invalid Station Id"
    if (name.isEmpty) error += "  | Invalid Name"
    if (latitude.isEmpty) error += "  | Invalid Latitude"
    if (longitude.isEmpty) error += " |  Invalid Longitude"

    error
  }

}

case class StationDataWithError(bikes_available: Option[Integer],
                                docks_available: Option[Integer],
                                is_renting: Boolean,
                                is_returning: Boolean,
                                last_updated: Long,
                                station_id: Option[String],
                                name: Option[String],
                                latitude: Option[Double],
                                longitude: Option[Double],
                                error: String
                               )

