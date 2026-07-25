package com.example.nextstoprealtime.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nextstoprealtime.model.Departure
import com.example.nextstoprealtime.model.Stop
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NextStopApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()

    NextStopTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (state.selectedStop != null) "Next Buses"
                            else "Next Stop Real Time"
                        )
                    },
                    navigationIcon = {
                        if (state.selectedStop != null) {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (state.selectedStop != null) {
                            IconButton(
                                onClick = { viewModel.loadDepartures() },
                                enabled = !state.isLoadingDepartures
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Data © bustimes.org • Open Government Licence",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.selectedStop == null) {
                    SearchScreen(
                        query = state.searchQuery,
                        stops = state.stops,
                        isSearching = state.isSearching || state.isLoadingNearby,
                        error = state.error,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onStopSelected = viewModel::selectStop,
                        onClearError = viewModel::clearError,
                        onNearbyRequested = { lat, lon -> viewModel.loadNearbyStops(lat, lon) }
                    )
                } else {
                    DeparturesScreen(
                        stop = state.selectedStop!!,
                        departures = state.departures,
                        isLoading = state.isLoadingDepartures,
                        error = state.error,
                        lastUpdated = state.lastUpdated,
                        onRefresh = viewModel::loadDepartures
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun SearchScreen(
    query: String,
    stops: List<Stop>,
    isSearching: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onStopSelected: (Stop) -> Unit,
    onClearError: () -> Unit,
    onNearbyRequested: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            permissionDenied = false
            fetchCurrentLocation(context, onNearbyRequested)
        } else {
            permissionDenied = true
        }
    }

    fun requestNearby() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation(context, onNearbyRequested)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search stop name or ATCO…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(8.dp))

        // Near me button (uses ACCESS_FINE_LOCATION)
        OutlinedButton(
            onClick = { requestNearby() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSearching
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (isSearching) "Finding nearby stops…" else "Near me (use my location)")
        }

        if (permissionDenied) {
            Text(
                "Location permission is required for nearby stops. Please grant it in Settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onClearError) { Text("Dismiss") }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (isSearching) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
        } else if (stops.isEmpty() && query.length >= 2) {
            Text(
                "No matching stops found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp)
            )
        } else if (query.length < 2 && stops.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.DirectionsBus,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Search for a UK bus stop",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Enter a stop name or ATCO code, or tap “Near me” to find stops around your current location (requires location permission).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stops, key = { it.atcoCode }) { stop ->
                    StopCard(stop = stop, onClick = { onStopSelected(stop) })
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: android.content.Context,
    onResult: (Double, Double) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()
    client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            }
        }
        .addOnFailureListener {
            // Fallback to last known location
            client.lastLocation.addOnSuccessListener { last ->
                if (last != null) {
                    onResult(last.latitude, last.longitude)
                }
            }
        }
}

@Composable
fun StopCard(stop: Stop, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stop.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (stop.subtitle.isNotBlank()) {
                Text(
                    text = stop.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stop.atcoCode,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun DeparturesScreen(
    stop: Stop,
    departures: List<Departure>,
    isLoading: Boolean,
    error: String?,
    lastUpdated: Long?,
    onRefresh: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = stop.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stop.atcoCode,
                    style = MaterialTheme.typography.bodySmall
                )
                if (lastUpdated != null) {
                    Text(
                        text = "Updated ${formatRelative(lastUpdated)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        when {
            isLoading && departures.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRefresh) { Text("Retry") }
                }
            }
            departures.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No upcoming departures")
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(departures, key = { it.id ?: it.hashCode().toLong() }) { dep ->
                        DepartureCard(dep)
                    }
                }
            }
        }
    }
}

@Composable
fun DepartureCard(departure: Departure) {
    val isLive = departure.live == true || departure.expectedDepartureTime != null
    val delayMinutes = departure.delay?.let { it / 60 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = departure.service?.lineName ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = departure.destination?.display ?: "Unknown destination",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val operator = departure.service?.operators?.firstOrNull()?.name
                if (!operator.isNullOrBlank()) {
                    Text(
                        text = operator,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = departure.vehicle != null) {
                    departure.vehicle?.let { v ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.DirectionsBus,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = v.display,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val expected = formatTime(departure.expectedDepartureTime)
                val aimed = formatTime(departure.aimedDepartureTime)

                if (expected != null) {
                    Text(
                        text = expected,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isLive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                    )
                    if (aimed != null && aimed != expected) {
                        Text(
                            text = "sched $aimed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (aimed != null) {
                    Text(
                        text = aimed,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isLive) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = when {
                                delayMinutes != null && delayMinutes > 0 -> "+${delayMinutes}m LIVE"
                                delayMinutes != null && delayMinutes < 0 -> "${delayMinutes}m LIVE"
                                else -> "LIVE"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return try {
        val instant = Instant.parse(iso)
        val local = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("HH:mm").format(local)
    } catch (_: Exception) {
        iso.takeLast(5)
    }
}

private fun formatRelative(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val mins = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(epochMs), Instant.ofEpochMilli(now))
    return when {
        mins < 1 -> "just now"
        mins == 1L -> "1 min ago"
        else -> "$mins mins ago"
    }
}
