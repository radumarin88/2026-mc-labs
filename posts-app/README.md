# Posts app (Labs 4 to 10)

This is the one app you build up across the productionization labs. You open this same `posts-app`
folder every lab; the code grows with you, one layer at a time: architecture, then networking, a
local database, offline sync, dependency injection, tests, and finally a mobile-computing feature.

## Which guide do I follow?

Open the guide for the lab you are on. Earlier guides stay here, so you can always look back at how
a layer was built.

- Lab 4, Architecture (ViewModel, UDF): [docs/lab4.md](docs/lab4.md)

More guides appear in `docs/` as the course reaches each lab.

## Setup, once

- **Android Studio** with the Android SDK (**Platform 37** installed). Open the `posts-app` folder
  and let Gradle sync (the first sync downloads the toolchain and libraries, so you need internet once).
- Run the fast tests with `./gradlew test`. Later labs add instrumented tests
  (`./gradlew connectedDebugAndroidTest`), which need a device or emulator running.
- Pinned tooling is in `gradle/libs.versions.toml`; each guide notes what its lab adds.

## The branch you are on

Your branch is your progress. A `labN-starter` branch has the exercises for lab N to fill in (with
every earlier lab already done); the matching `labN-solution` is the reference solution for lab N.
