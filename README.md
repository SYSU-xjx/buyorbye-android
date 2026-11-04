# buy or bye Android

Java Android client for collaborative spending approvals and purchase decisions.

## Requirements

- Android Studio with Android SDK Platform 37
- JDK 17 (Android Studio's bundled JDK is suitable)
- An emulator or device running Android 7.0 (API 24) or later

## Run

Open this directory in Android Studio, sync Gradle, then run the `app` configuration on an emulator or device. The Android Studio project created the Gradle Wrapper, so command-line checks run with:

```sh
./gradlew test lint assembleDebug
```

The debug API URL defaults to `http://10.0.2.2:8080/api/` for the Android emulator. For a physical device, set `API_BASE_URL` in `app/build.gradle.kts` to the development machine's LAN address. Do not commit secrets or device-specific URLs.

## Structure and versions

- Application ID and Java namespace: `com.hku.money`
- Java 17, `minSdk 24`, `compileSdk 37`, `targetSdk 37`
- One Activity with four Navigation Component Fragment destinations: Home, Plaza, Groups, Profile
- AndroidX Navigation, ViewModel/LiveData, Material XML, Retrofit/OkHttp/Gson, and Room are configured
- Room is reserved for local decision-wheel data; the business schema and API models follow team review

## Team workflow

- Branch from `main` with a focused name such as `feature/approval-list`.
- Open a Pull Request; do not push directly to `main`.
- Require one teammate approval and passing Android CI before merging.
- Follow `.editorconfig` and keep API changes coordinated with the backend repository.
- Never commit `local.properties`, credentials, signing keys, IDE state, or build output.

See [CONTRIBUTING.md](CONTRIBUTING.md).
