# Schwifty Savings — POE Part 2

Kotlin Android budget tracker prototype (Compose + Room).

## Students
- Abhijith Naidoo / ST10453249
- Module: OPSC7311

## Features
- Username/password register + login (Room + hashed passwords)
- Categories
- Expenses: date, time, description, category, amount, optional photo
- Min/max monthly goals (SeekBar)
- Transactions filtered by selectable period + photo access
- Category totals by period
- Leaderboard
- Local RoomDB (`schwifty_savings.db`)

## Demo video
Unlisted YouTube link: **PASTE LINK HERE**

## How to run
1. Clone this repo
2. Open in Android Studio
3. Sync Gradle
4. Run on emulator API 26+

## Tests / CI
- Unit tests: `./gradlew test` (or Run `CoreLogicTest` in Android Studio)
- GitHub Actions: `.github/workflows/android-ci.yml` (green on `main`)

## APK
Debug APK available from GitHub Actions **Artifacts** (`app-debug`) and locally at:
`app/build/outputs/apk/debug/app-debug.apk`

## Reference list

Android Developers, 2024. Intents and intent filters. [online] Available at: `<https://developer.android.com/guide/components/intents-filters>` [Accessed 11 September 2026].

Android Developers, 2024. Jetpack Compose tutorial. [online] Available at: `<https://developer.android.com/develop/ui/compose/tutorial>` [Accessed 12 September 2026].

Android Developers, 2024. Save data in a local database using Room. [online] Available at: `<https://developer.android.com/training/data-storage/room>` [Accessed 09 September 2026].

GitHub, 2024. Building and testing Java with Gradle. [online] Available at: `<https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle>` [Accessed 16 September 2026].

Oracle, 2021. Class NumberFormat (Java SE 17 & JDK 17). [online] Available at: `<https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/NumberFormat.html>` [Accessed 14 September 2026].
