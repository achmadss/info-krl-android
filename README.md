# Info KRL Android

An Android application for tracking Indonesian commuter train (KRL) schedules in real-time. Pin your favorite stations and get instant access to departure times, train information, and route details.

> This project is based on the design and concept from [Comuline](https://github.com/comuline/web), the original web application by the Comuline team.

## ✨ Features

- **Station Pinning**: Pin your frequently used stations for quick access
- **Real-time Schedules**: View upcoming train departures with live countdowns
- **Pull-to-Refresh**: Refresh individual station schedules with a simple swipe
- **Smart Sync**: Automatic background synchronization of schedule data
- **Route Information**: Detailed train routes with stop counts and ETAs
- **Search**: Quickly find specific destinations across all your pinned stations
- **Tabbed Interface**: Easy navigation between multiple pinned stations
- **Material Design 3**: Modern, clean UI following Material Design guidelines

## 🏗️ Architecture

This project follows a clean, modular architecture with the following modules:

- **`app`** - Main application module with UI screens and Jetpack Compose components
- **`core`** - Core utilities, extensions, and shared functionality
- **`domain`** - Business logic, blueprints and domain models
- **`data`** - Data sources, repositories, and API integrations

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Navigation**: Voyager
- **Dependency Injection**: Koin
- **Background Tasks**: WorkManager
- **Async**: Kotlin Coroutines & Flow
- **Image Loading**: Coil

## 📱 Screenshots

The app features:
- Home screen with tabbed station views
- Schedule listings with color-coded train lines
- Detailed departure information including train IDs and stop counts
- Pull-to-refresh functionality on each station tab
- Search functionality across all stations

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- Android SDK 26 or higher
- Gradle 8.x

### Building

```bash
# Clone the repository
git clone https://github.com/yourusername/info-krl-android.git
cd info-krl-android

# Build the project
./gradlew build

# Install on device/emulator
./gradlew installDebug
```

## 📁 Project Structure

```
info-krl-android/
├── app/                          # Main application module
│   ├── screens/                  # Screen composables and view models
│   ├── components/               # Reusable UI components
│   ├── work/                     # Background sync jobs
│   ├── util/                     # Utility functions
│   └── di/                       # Dependency injection
├── core/                         # Core utilities module
├── domain/                       # Business logic module
└── data/                         # Data layer module
```

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the GNU Affero General Public License v3.0 (AGPLv3) - see the [LICENSE](LICENSE) file for details.