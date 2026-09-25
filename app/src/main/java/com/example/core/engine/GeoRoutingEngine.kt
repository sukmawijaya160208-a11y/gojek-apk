package com.example.core.engine

import com.example.core.model.ServiceType
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-precision Geographical Distance & ETA Computation Engine.
 * Uses the spherical Haversine formula and city road detour factor for realistic urban navigation.
 */
object GeoRoutingEngine {

  private const val EARTH_RADIUS_KM = 6371.0
  private const val CITY_DETOUR_FACTOR = 1.25 // Factor to convert great-circle distance to actual road distance

  /**
   * Calculates the straight-line Haversine distance in kilometers between two GPS coordinates.
   */
  fun calculateHaversineDistanceKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
  ): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val rLat1 = Math.toRadians(lat1)
    val rLat2 = Math.toRadians(lat2)

    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(rLat1) * cos(rLat2) *
        sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
  }

  /**
   * Calculates realistic road network driving distance in kilometers.
   */
  fun calculateRoadDistanceKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double,
    detourFactor: Double = CITY_DETOUR_FACTOR
  ): Double {
    val haversine = calculateHaversineDistanceKm(lat1, lon1, lat2, lon2)
    val roadDist = haversine * detourFactor
    // Round to 1 decimal place
    return Math.round(roadDist * 10.0) / 10.0
  }

  /**
   * Computes the Estimated Time of Arrival (ETA) in minutes based on service type speed profiles
   * and traffic congestion modifier.
   *
   * Motor (Ride Bike / Delivery): 28 km/h average city speed
   * Mobil (Ride Car): 20 km/h average city speed
   */
  fun estimateDurationMinutes(
    distanceKm: Double,
    serviceType: ServiceType,
    isPeakHour: Boolean = false
  ): Int {
    val speedKmh = when (serviceType) {
      ServiceType.RIDE_BIKE, ServiceType.DELIVERY_BIKE -> 28.0
      ServiceType.RIDE_CAR -> 20.0
      ServiceType.FOOD, ServiceType.SHOP -> 25.0
    }

    val trafficMultiplier = if (isPeakHour) 1.35 else 1.15
    val hours = (distanceKm / speedKmh) * trafficMultiplier
    val minutes = (hours * 60.0).toInt()

    // Minimum ETA is 5 minutes to account for staging and traffic lights
    return maxOf(5, minutes)
  }

  /**
   * Generates interpolated geographic waypoints between start and end coordinates.
   * Useful for smooth route simulation and map rendering.
   */
  fun generateRouteWaypoints(
    startLat: Double,
    startLng: Double,
    endLat: Double,
    endLng: Double,
    stepCount: Int = 10
  ): List<Pair<Double, Double>> {
    val waypoints = mutableListOf<Pair<Double, Double>>()
    for (i in 0..stepCount) {
      val fraction = i.toDouble() / stepCount.toDouble()
      val lat = startLat + (endLat - startLat) * fraction
      val lng = startLng + (endLng - startLng) * fraction
      waypoints.add(lat to lng)
    }
    return waypoints
  }

  fun formatDistance(distanceKm: Double): String {
    return if (distanceKm < 1.0) {
      "${(distanceKm * 1000).toInt()} m"
    } else {
      String.format(java.util.Locale.US, "%.1f km", distanceKm)
    }
  }

  fun formatDuration(minutes: Int): String {
    return if (minutes < 60) {
      "$minutes menit"
    } else {
      val hours = minutes / 60
      val remainingMin = minutes % 60
      if (remainingMin == 0) "$hours jam" else "$hours jam $remainingMin mnt"
    }
  }
}
