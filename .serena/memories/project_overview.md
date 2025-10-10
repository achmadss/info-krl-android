# Comuline - Android Project

## Purpose
Comuline is an Android application for tracking train station schedules. Users can:
- Pin favorite stations
- View schedules for different destinations from each station
- Sync schedule data for their favorited stations
- Filter and search schedules

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Multi-module (app, core, domain, data)
- **Navigation**: Voyager (navigator, tabNavigator, transitions, screenmodel)
- **DI**: Koin
- **Background Jobs**: WorkManager
- **Async**: Coroutines + Flow
- **Minimum SDK**: 26
- **Target SDK**: 36

## Key Dependencies
- Material3 + Material Icons
- Coil for image loading
- Voyager for navigation and screen models
- WorkManager for background sync jobs
- Core Splashscreen

## Project Structure
```
app/              - Main UI and screens
├── screens/      - Screen composables and screen models
│   ├── home/     - Home screen with tabbed station schedules
│   ├── stations/ - Station selection screen
├── components/   - Reusable UI components
├── work/         - WorkManager jobs for syncing
├── di/           - Dependency injection modules
├── util/         - Utilities
├── theme/        - Compose theme
├── base/         - MainActivity, Application
core/             - Core utilities and extensions
domain/           - Business logic and use cases
data/             - Data sources and repositories
```
