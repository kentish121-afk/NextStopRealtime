# Next Stop Real Time

An Android app that shows the **next 5 buses** departing from any UK bus stop using public data from [bustimes.org](https://bustimes.org).

## Features

- **Search stops** by name or ATCO code
- **Near me** – uses device location to find nearby stops
- **Real-time departures** with LIVE predictions and delay info
- **Vehicle allocation** (fleet code / registration) when AVL data is available
- **Arrival reminders** – tap “Notify me 5 min before” on any departure to get a notification
- Clean Material 3 UI

## Permissions

| Permission | Purpose |
|------------|---------|
| `INTERNET` / `ACCESS_NETWORK_STATE` | API calls |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | “Near me” feature |
| `ACCESS_BACKGROUND_LOCATION` | Allows future continuous nearby monitoring (requested only after fine location is granted) |
| `POST_NOTIFICATIONS` (Android 13+) | Arrival reminder notifications |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule reminders after reboot |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Precise reminder timing |

## How reminders work

1. Open a stop and view the next buses.
2. On any departure that has a time, tap **Notify me 5 min before**.
3. The app schedules an exact alarm.
4. When the time arrives you get a high-priority notification with the line and destination.

## Data Sources

- bustimes.org (`/api/stops/`, `/stops.json`, `/stops/{atco}/times.json`, vehicle journeys)
- Live AVL from BODS, Welsh Bus Data Service, Ember, etc.

Please be respectful of the API – the site is volunteer-run.

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Retrofit + kotlinx.serialization
- Google Play Services Location
- AlarmManager for reminders

## Build & Run

1. Open in Android Studio
2. Sync Gradle
3. Run on device/emulator (API 26+)

## Licence

Educational / demonstration purposes. Bus data remains under the original open licences. Credit bustimes.org if you ship a derivative.
