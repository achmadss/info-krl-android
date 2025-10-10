# Code Style & Conventions

## Naming Conventions
- **Classes**: PascalCase (e.g., `HomeScreen`, `SyncScheduleJob`)
- **Functions/Methods**: camelCase (e.g., `fetchSchedules`, `onTabFocused`)
- **Properties**: camelCase
- **Private properties**: prefixed with underscore (e.g., `_focusedStationId`)
- **Composables**: PascalCase (same as classes)
- **Constants**: UPPER_SNAKE_CASE in companion objects

## Compose Patterns
- Screen objects extend `Screen` from Voyager
- Screen models extend `ScreenModel`
- State is managed with `MutableStateFlow` and exposed as `StateFlow`
- Private mutable state with public read-only access pattern
- Use `collectAsState()` to consume flows in composables

## Architecture Patterns
- Screen Models handle business logic and state
- Repositories injected via Koin
- WorkManager jobs for background sync operations
- Flows for reactive data streams
- Separation between screen object (with `Content()`) and screen composable function
