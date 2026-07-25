# Next Stop Real Time

An Android app that shows the **next 5 buses** departing from any UK bus stop using public data from [bustimes.org](https://bustimes.org).

## Features

- **Search stops** by name or ATCO code (via `/api/stops/`)
- **Near me** – uses `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` to find nearby stops via the `/stops.json` bounding-box endpoint
- **Real-time departures** for the selected stop (via `/stops/{atco}/times.json?limit=5`)
  - Scheduled (aimed) departure times
  - Expected / predicted times when live AVL data is available
  - Delay information and LIVE badge
- **Vehicle allocation** when AVL (Automatic Vehicle Location) data is present:
  - Fleet code / fleet number
  - Vehicle registration
  - Looked up via vehicle journeys when the departure is live and has a `trip_id`
- Clean Material 3 UI with Jetpack Compose
- Auto-refresh button and relative “last updated” time
- Proper attribution to bustimes.org / open data sources

## Permissions

- `INTERNET` / `ACCESS_NETWORK_STATE` – required for API calls
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` – optional, only used when the user taps **Near me**

Runtime permission is requested only when the user chooses the location feature.

## Data Sources

- **bustimes.org** public JSON endpoints (undocumented but stable `times.json` used by the website itself, plus the official REST API for stops & vehicles, and `/stops.json` for geographic queries)
- Live vehicle tracking comes from BODS, Welsh Bus Data Service, Ember and other AVL feeds aggregated by bustimes.org
- Timetables from Traveline / BODS / TfL etc. under Open Government Licence

**Please be respectful of the API** – the site is run by a volunteer. Add a sensible User-Agent (already included) and avoid aggressive polling.

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Retrofit + OkHttp + kotlinx.serialization
- Coroutines + ViewModel + StateFlow
- Google Play Services Location
- Minimum SDK 26

## How to Build & Run

1. Open the project in **Android Studio** (Hedgehog or newer recommended).
2. Let Gradle sync (it will download dependencies).
3. Connect a device / start an emulator (API 26+).
4. Run the `app` configuration.

No API key is required.

## Project Structure

```
app/src/main/java/com/example/nextstoprealtime/
├── MainActivity.kt
├── model/          # Data classes (Stop, Departure, VehicleInfo, GeoJSON…)
├── network/        # Retrofit API definition
├── data/           # Repository (search, nearby, departures + vehicle enrichment)
└── ui/             # Compose screens, ViewModel, theme
```

## Notes / Limitations

- The `times.json` and `stops.json` endpoints are not part of the officially documented REST API root, but are publicly used by the website and several third-party projects.
- Vehicle details appear only when real-time AVL is available for that trip and the lookup succeeds.
- Nearby search uses a small bounding box (~1 km). The API limits box size.
- No offline caching of stops (could be added with Room).

## Licence

This sample app is provided as-is for educational / demonstration purposes.  
Bus data remains under the licences of the original providers (primarily Open Government Licence v3.0).  
Please credit bustimes.org if you ship a derivative.

Enjoy the next bus!
