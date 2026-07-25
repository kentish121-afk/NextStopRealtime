package com.example.nextstoprealtime.data

import com.example.nextstoprealtime.model.Departure
import com.example.nextstoprealtime.model.Stop
import com.example.nextstoprealtime.network.BustimesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos

class BustimesRepository(
    private val api: BustimesApi = BustimesApi.create()
) {

    suspend fun searchStops(query: String): Result<List<Stop>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) return@withContext Result.success(emptyList())
            val response = api.searchStops(query.trim())
            Result.success(response.results.filter { it.active != false })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch stops near a lat/lon using the /stops.json bounding-box endpoint.
     * Creates a ~1.2 km box (respects the API's ~0.15 sq degree area limit).
     */
    suspend fun getNearbyStops(latitude: Double, longitude: Double, radiusDegrees: Double = 0.008): Result<List<Stop>> =
        withContext(Dispatchers.IO) {
            try {
                // Adjust longitude delta for latitude (approx)
                val lonDelta = radiusDegrees / cos(Math.toRadians(latitude)).coerceAtLeast(0.3)
                val ymin = latitude - radiusDegrees
                val ymax = latitude + radiusDegrees
                val xmin = longitude - lonDelta
                val xmax = longitude + lonDelta

                val geo = api.getStopsInBounds(ymin, ymax, xmin, xmax)
                val stops = geo.features.mapNotNull { feature ->
                    val props = feature.properties ?: return@mapNotNull null
                    val atco = props.atcoCode ?: return@mapNotNull null
                    val coords = feature.geometry?.coordinates
                    Stop(
                        atcoCode = atco,
                        name = props.name,
                        commonName = props.name,
                        longName = props.name,
                        location = coords,
                        indicator = props.indicator,
                        bearing = props.bearing?.toString(),
                        lineNames = props.services,
                        active = true
                    )
                }
                Result.success(stops)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getNextDepartures(atcoCode: String, limit: Int = 5): Result<List<Departure>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getStopTimes(atcoCode, limit)
                // Enrich with vehicle info when possible for live trips
                val enriched = response.times.map { dep ->
                    if (dep.vehicle == null && dep.tripId != null && dep.live == true) {
                        try {
                            val journeys = api.getVehicleJourneys(dep.tripId, 3)
                            val matching = journeys.results.firstOrNull { it.tripId == dep.tripId }
                            if (matching?.vehicle != null) {
                                dep.copy(vehicle = matching.vehicle)
                            } else dep
                        } catch (_: Exception) {
                            dep
                        }
                    } else dep
                }
                Result.success(enriched)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
