# Next Stop Real Time

An Android app that shows the **next 5 buses** departing from any UK bus stop using public data from [bustimes.org](https://bustimes.org).

## Features

- **Search stops** by name or ATCO code (via `/api/stops/`)
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

## Data Sources

- **bustimes.org** public JSON endpoints (undocumented but stable `times.json` used by the website itself, plus the official REST API for stops & vehicles)
- Live vehicle tracking comes from BODS, Welsh Bus Data Service, Ember and other AVL feeds aggregated by bustimes.org
- Timetables from Traveline / BODS / TfL etc. under Open Government Licence

**Please be respectful of the API** – the site is run by a volunteer. Add a sensible User-Agent (already included) and avoid aggressive polling.

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Retrofit + OkHttp + kotlinx.serialization
- Coroutines + ViewModel + StateFlow
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
├── model/          # Data classes (Stop, Departure, VehicleInfo…)
├── network/        # Retrofit API definition
├── data/           # Repository (enrichment of live vehicle data)
└── ui/             # Compose screens, ViewModel, theme
```

## Notes / Limitations

- The `times.json` endpoint is not part of the officially documented REST API root, but is publicly used by the website and several third-party projects.
- Vehicle details appear only when real-time AVL is available for that trip and the lookup succeeds.
- Location-based “nearby stops” is not implemented (would require ACCESS_FINE_LOCATION + reverse geocoding or a local NaPTAN database). Search works well for most use cases.
- No offline caching of stops (could be added with Room).

## Licence

This sample app is provided as-is for educational / demonstration purposes.  
Bus data remains under the licences of the original providers (primarily Open Government Licence v3.0).  
Please credit bustimes.org if you ship a derivative.

Enjoy the next bus!
