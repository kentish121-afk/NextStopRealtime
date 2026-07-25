package com.example.nextstoprealtime.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopSearchResponse(
    val next: String? = null,
    val previous: String? = null,
    val results: List<Stop> = emptyList()
)

@Serializable
data class Stop(
    @SerialName("atco_code") val atcoCode: String,
    @SerialName("naptan_code") val naptanCode: String? = null,
    @SerialName("common_name") val commonName: String? = null,
    val name: String? = null,
    @SerialName("long_name") val longName: String? = null,
    val location: List<Double>? = null, // [lon, lat]
    val indicator: String? = null,
    val bearing: String? = null,
    @SerialName("line_names") val lineNames: List<String>? = null,
    val active: Boolean? = null
) {
    val displayName: String
        get() = longName ?: name ?: commonName ?: atcoCode

    val subtitle: String
        get() = buildString {
            if (!indicator.isNullOrBlank()) append(indicator)
            if (!bearing.isNullOrBlank()) {
                if (isNotEmpty()) append(" • ")
                append(bearing)
            }
            if (!lineNames.isNullOrEmpty()) {
                if (isNotEmpty()) append(" • ")
                append(lineNames.take(5).joinToString(", "))
            }
        }
}

@Serializable
data class TimesResponse(
    val times: List<Departure> = emptyList()
)

@Serializable
data class Departure(
    val id: Long? = null,
    @SerialName("trip_id") val tripId: Long? = null,
    val service: Service? = null,
    val destination: Destination? = null,
    @SerialName("aimed_arrival_time") val aimedArrivalTime: String? = null,
    @SerialName("aimed_departure_time") val aimedDepartureTime: String? = null,
    @SerialName("expected_departure_time") val expectedDepartureTime: String? = null,
    val live: Boolean? = null,
    val progress: Double? = null,
    val delay: Int? = null, // seconds
    // Vehicle may appear in some live responses
    val vehicle: VehicleInfo? = null
)

@Serializable
data class Service(
    @SerialName("line_name") val lineName: String? = null,
    val operators: List<Operator>? = null
)

@Serializable
data class Operator(
    val id: String? = null,
    val name: String? = null,
    @SerialName("vehicle_mode") val vehicleMode: String? = null,
    val parent: String? = null
)

@Serializable
data class Destination(
    @SerialName("atco_code") val atcoCode: String? = null,
    val name: String? = null,
    val locality: String? = null
) {
    val display: String
        get() = listOfNotNull(name, locality).joinToString(", ").ifBlank { "Unknown" }
}

@Serializable
data class VehicleInfo(
    val id: Long? = null,
    val slug: String? = null,
    @SerialName("fleet_code") val fleetCode: String? = null,
    val reg: String? = null,
    @SerialName("fleet_number") val fleetNumber: String? = null
) {
    val display: String
        get() = buildString {
            if (!fleetCode.isNullOrBlank()) append(fleetCode)
            else if (!fleetNumber.isNullOrBlank()) append(fleetNumber)
            if (!reg.isNullOrBlank()) {
                if (isNotEmpty()) append(" • ")
                append(reg)
            }
        }.ifBlank { "Unknown vehicle" }
}

// For vehicle journeys lookup if needed
@Serializable
data class VehicleJourneyResponse(
    val next: String? = null,
    val previous: String? = null,
    val results: List<VehicleJourney> = emptyList()
)

@Serializable
data class VehicleJourney(
    val id: Long? = null,
    val datetime: String? = null,
    val vehicle: VehicleInfo? = null,
    @SerialName("route_name") val routeName: String? = null,
    val destination: String? = null,
    @SerialName("trip_id") val tripId: Long? = null
)
