# YALA — Your App Lock and Analyzer

**Developed by MNM YOUNUS**
100% Offline · Zero Ads · Zero Internet Permissions

## What this project is

A Kotlin + Jetpack Compose Android app skeleton implementing:

- Clean Architecture (`domain` / `data` / `presentation`) with MVVM, `StateFlow`, and Hilt DI
- Four custom lock types: 4-digit PIN, 8-character password, pattern, and a
  5-image pool with a secret 3–4 image unlock sequence
- A 12-character emergency recovery key (PBKDF2-hashed, never stored in plaintext)
- Per-app or global lock scope, including system apps (e.g. Settings)
- An `AccessibilityService` that detects foreground-app changes and shows a
  full-screen lock challenge (`LockOverlayActivity`) before the target app is visible
- A `DeviceAdminReceiver` so the app can't be silently force-stopped or uninstalled
- Local-only "intruder capture": a silent front-camera photo on every unlock
  attempt, filed into on-device Successful / Failed galleries — nothing is
  uploaded, shared, or written to `MediaStore`
- Light / Dark / System theming, with a `tvFocusable()` modifier and TV
  manifest entries (`LEANBACK_LAUNCHER`, `android.software.leanback`) for D-pad navigation
- A step-by-step onboarding flow covering every permission and a privacy disclaimer
- A GitHub Actions workflow that builds an **unsigned debug APK** on every push to `main` or `v*` tag

## Why some things are stubbed

This is a scaffold meant to hand a development team (or an IDE + AI pairing
session) a correct architecture and working core logic — not a
10,000-file production app generated in one pass. A few things are
intentionally left as clearly-marked TODOs or simplified:

- `SettingsViewModel`'s permission-status intents duplicate `OnboardingViewModel`'s —
  in a real build these should be pulled into a shared `PermissionsRepository`.
- Recovery-key re-entry inside `LockOverlayActivity.onRequestRecovery` is wired
  as a callback but not yet routed to a dedicated recovery-challenge screen.
- `IntruderRepositoryImpl.deleteCapture` doesn't yet delete the backing file —
  the entity delete is wired, the file cleanup is a one-line follow-up.
- The default-foreground-launcher check in `AppLockAccessibilityService` uses
  a placeholder package name; production code should resolve this dynamically
  via `PackageManager.resolveActivity(Intent(ACTION_MAIN).addCategory(CATEGORY_HOME))`.

## Why there's no `gradlew` binary committed

This project intentionally omits a committed `gradle-wrapper.jar`. That file
is a binary blob normally downloaded from `services.gradle.org`, and it
should never be hand-written or improvised — either generate it yourself,
or let Android Studio do it:

```bash
# Requires a local Gradle install (or Android Studio, which bundles one)
gradle wrapper --gradle-version 8.7
```

Alternatively just open the project root in Android Studio — it will offer
to generate the wrapper automatically on first sync. `gradle/wrapper/gradle-wrapper.properties`
is already included so the generated wrapper points at the right Gradle version.

CI (`.github/workflows/build.yml`) doesn't depend on the wrapper at all — it
uses `gradle/actions/setup-gradle` to provision Gradle 8.7 directly on the
runner and calls `gradle assembleDebug`, so builds work from a fresh clone
with no extra setup.

## Building locally

```bash
gradle wrapper --gradle-version 8.7   # one-time, see above
./gradlew assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/`.

## Permissions declared (and the one that's deliberately absent)

`CAMERA`, `SYSTEM_ALERT_WINDOW`, `PACKAGE_USAGE_STATS`, `QUERY_ALL_PACKAGES`,
`RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`(+`_SPECIAL_USE`), `WAKE_LOCK`,
`VIBRATE`, plus the `BIND_ACCESSIBILITY_SERVICE` and `BIND_DEVICE_ADMIN`
system permissions tied to the two declared components.

**No `INTERNET` permission is declared anywhere in the manifest.** Combined
with no analytics/crash-reporting SDKs anywhere in `build.gradle.kts`, the
app has no code path capable of sending data off-device.
