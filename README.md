# Schwifty Savings — POE Part 3

Schwifty Savings is an Android budget tracker. It helps a person register, log expenses, set a minimum and a maximum spending goal, and see whether they are staying inside that band. The interface uses one forest-green and mint palette, a bottom bar for the main sections, and a back button on every detail screen.

## Students

- Abhijith Naidoo / ST10453249
- Barrie-George Davies / ST10449636
- Tristan Robinson / ST10439581
- Aneaho Nemakonde / ST10442088
- Module: OPSC7311

## Design

The app keeps the Part 2 layout: mint background, forest headers, and rounded cards. New Part 3 screens use the same colours and the same back-button pattern so the graph, goal progress, and badges feel like part of the existing app.

The launcher icon was drawn in Figma as a 1024 by 1024 forest square with the letters SS, then imported with Android Studio Image Asset.

## Features marked for Part 3



### Own feature 1 — Leaderboard

The Leaderboard tab ranks the signed-in user against sample savers by the percentage of the maximum goal they still have left. Opening the tab writes that user's score to the Firestore collection `leaderboard`. The screen shows the line "Your score is stored online" after a successful write.

### Own feature 2 — Badges and logging streaks

Settings → Badges shows six badges. They unlock only from behaviour in the local budget data:

- First Entry: at least one expense
- Receipt Keeper: an expense with a photo
- 3-day streak and 7-day streak: expenses on consecutive days
- Goal Setter: a saved minimum and maximum
- In the Zone: spending over the last 30 days is between the minimum and the maximum

The earned list is written to the Firestore collection `badges`.

### Required Part 3 features

- Spending graph (Settings, or Category totals → View spending graph). Bars are category totals for a period the user picks. The green line is the minimum goal and the red line is the maximum goal.
- Goal progress (Settings). A bar for the last 30 days. The green band is the gap between minimum and maximum. The dark line is the amount spent. The heading says below, inside, or over the goals.
- Online database. Cloud Firestore stores users, categories, expenses, goals, badges, and the leaderboard score. The phone still uses Room so the screens keep working offline. Register, login, and each save also write to Firestore. Login and opening the home screen read Firestore back into Room, so a second phone can sign in with the same username and password.



## GitHub and GitHub Actions

The source is this repository. Work is committed to `main` as features are finished.

GitHub Actions workflow: `.github/workflows/android-ci.yml`

On every push to `main` the workflow:

1. Checks out the code.
2. Installs JDK 17 and Android SDK 35.
3. Runs `./gradlew test` (unit tests, including goal-zone and badge rules).
4. Runs `./gradlew assembleDebug`.
5. Uploads the debug APK as the artifact `app-debug`.

`app/google-services.json` is committed so that workflow can apply the Google services plugin. A missing file fails the build.

## Demo video

Recorded on a physical Android phone, with a voiceover. The video shows login, an expense being saved, that expense appearing in the Firebase console, the spending graph, goal progress, badges, and the leaderboard.

Unlisted YouTube link: [https://youtu.be/kZvyEpQuKP0](https://youtu.be/kZvyEpQuKP0) 

## How to run

1. Clone this repo.
2. Open the folder that contains `settings.gradle.kts` in Android Studio.
3. Sync Gradle.
4. Run on a physical Android phone, API 26 or higher. Enable Developer options and USB debugging, accept the RSA prompt, then choose the phone in the device dropdown.



## APK

Debug APK:

`app/build/outputs/apk/debug/app-debug.apk`

Build it with **Build → Generate App Bundles or APKs → Generate APKs**, or run `.\gradlew.bat assembleDebug` from the project root.

The same APK is the GitHub Actions artifact named `app-debug`.

## Reference list

Android Developers, 2024. Intents and intent filters. [online] Available at: [https://developer.android.com/guide/components/intents-filters](https://developer.android.com/guide/components/intents-filters) [Accessed 11 September 2026].

Android Developers, 2024. Jetpack Compose tutorial. [online] Available at: [https://developer.android.com/develop/ui/compose/tutorial](https://developer.android.com/develop/ui/compose/tutorial) [Accessed 12 September 2026].

Android Developers, 2024. Save data in a local database using Room. [online] Available at: [https://developer.android.com/training/data-storage/room](https://developer.android.com/training/data-storage/room) [Accessed 09 September 2026].

Firebase, 2026. Add data to Cloud Firestore. [online] Available at: [https://firebase.google.com/docs/firestore/manage-data/add-data](https://firebase.google.com/docs/firestore/manage-data/add-data) [Accessed 26 October 2026].

Firebase, 2026. Get data with Cloud Firestore. [online] Available at: [https://firebase.google.com/docs/firestore/query-data/get-data](https://firebase.google.com/docs/firestore/query-data/get-data) [Accessed 26 October 2026].

GitHub, 2024. Building and testing Java with Gradle. [online] Available at: [https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle) [Accessed 16 September 2026].

Oracle, 2021. Class NumberFormat (Java SE 17 & JDK 17). [online] Available at: [https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/NumberFormat.html](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/NumberFormat.html) [Accessed 14 September 2026].