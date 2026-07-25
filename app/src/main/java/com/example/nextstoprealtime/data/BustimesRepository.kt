package com.example.nextstoprealtime.data

import com.example.nextstoprealtime.model.Departure
import com.example.nextstoprealtime.model.Stop
import com.example.nextstoprealtime.model.VehicleInfo
import com.example.nextstoprealtime.network.BustimesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
