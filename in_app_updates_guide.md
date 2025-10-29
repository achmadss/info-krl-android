# Support In-App Updates (Kotlin or Java)

## Overview

This guide describes how to support in-app updates in your app using either Kotlin or Java. There are separate guides for native code (C/C++) and for Unity or Unreal Engine implementations.

---

## Set Up Your Development Environment

The Play In-App Update Library is part of the Google Play Core libraries. Add the following Gradle dependency to your project:

```kotlin
// In your app's build.gradle.kts file:
dependencies {
    // This dependency is downloaded from Google's Maven repository.
    // Ensure you have that repository included in your project's build.gradle file.
    implementation("com.google.android.play:app-update:2.1.0")

    // Kotlin extensions for Play In-App Update
    implementation("com.google.android.play:app-update-ktx:2.1.0")
}
```

---

## Check for Update Availability

Before requesting an update, check if one is available using `AppUpdateManager`:

```kotlin
val appUpdateManager = AppUpdateManagerFactory.create(context)
val appUpdateInfoTask = appUpdateManager.appUpdateInfo

appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
        // Request the update
    }
}
```

The returned `AppUpdateInfo` instance contains the update status and intent if applicable.

---

## Check Update Staleness

Use `clientVersionStalenessDays()` to check how long ago an update became available:

```kotlin
val appUpdateManager = AppUpdateManagerFactory.create(context)
val appUpdateInfoTask = appUpdateManager.appUpdateInfo

appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE &&
        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
        // Request flexible update
    }
}
```

---

## Check Update Priority

You can assign an update priority (0–5) in the Google Play Developer API:

Example JSON for setting high priority (5):

```json
{
  "releases": [{
    "versionCodes": ["88"],
    "inAppUpdatePriority": 5,
    "status": "completed"
  }]
}
```

To check in your app:

```kotlin
val appUpdateManager = AppUpdateManagerFactory.create(context)
val appUpdateInfoTask = appUpdateManager.appUpdateInfo

appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        appUpdateInfo.updatePriority() >= 4 &&
        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
        // Request immediate update
    }
}
```

---

## Start an Update

Request an update using:

```kotlin
appUpdateManager.startUpdateFlowForResult(
    appUpdateInfo,
    activityResultLauncher,
    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
)
```

Register your result launcher using `ActivityResultContracts.StartIntentSenderForResult`.

---

## Configure Update Options

You can allow asset pack deletion if needed:

```kotlin
AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
    .setAllowAssetPackDeletion(true)
    .build()
```

Use this option carefully to avoid accidental asset loss.

---

## Get Callback for Update Status

```kotlin
registerForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
    if (result.resultCode != RESULT_OK) {
        log("Update flow failed: ${result.resultCode}")
    }
}
```

---

## Handle Flexible Updates

### Monitor Update Progress

```kotlin
val listener = InstallStateUpdatedListener { state ->
    if (state.installStatus() == InstallStatus.DOWNLOADING) {
        val progress = state.bytesDownloaded().toFloat() / state.totalBytesToDownload()
    }
}
appUpdateManager.registerListener(listener)
appUpdateManager.unregisterListener(listener)
```

### Install Flexible Update

```kotlin
val listener = { state: InstallState ->
    if (state.installStatus() == InstallStatus.DOWNLOADED) {
        popupSnackbarForCompleteUpdate()
    }
}

fun popupSnackbarForCompleteUpdate() {
    Snackbar.make(
        findViewById(R.id.activity_main_layout),
        "An update has just been downloaded.",
        Snackbar.LENGTH_INDEFINITE
    ).apply {
        setAction("RESTART") { appUpdateManager.completeUpdate() }
        show()
    }
}
```

Also, check on `onResume()` to prompt installation when ready.

---

## Handle Immediate Updates

If the update is in progress when returning to the app:

```kotlin
override fun onResume() {
    super.onResume()
    appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                activityResultLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }
    }
}
```

If a user cancels or declines the update, decide whether to continue or re-prompt later.
