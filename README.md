# Chirpie 2

## Summary
Chirpie 2 is a native Android encyclopedia and field guide application for birds. It is designed to catalog bird species with robust multilingual support (including English, Romanized Japanese, Kanji, Czech, and Scientific names) alongside multimedia elements like high-quality images and audio chirps. 

Repository: [https://github.com/hanenashi/chirpie2.git](https://github.com/hanenashi/chirpie2.git)

## Tech Proposition
To ensure modern performance, maintainability, and scalability, Chirpie 2 is built using the latest Android development standards:
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel) for clear separation of concerns
* **Local Storage:** Room Database (SQLite wrapper) for fast, queryable local data access
* **Image Loading:** Coil (lightweight, Kotlin-first image loading)
* **Audio:** Native Android `MediaPlayer` API

## Required Software (Windows 11)
To build, compile, and run this project on a Windows 11 machine, you will need the following installed:
1. **Android Studio:** The official IDE (download the latest stable version, which bundles the necessary JDK).
2. **Git for Windows:** To clone, commit, and push to this repository.
3. **Android Emulator / Physical Device:** Set up an emulator via Android Studio's Device Manager, or enable "USB Debugging" on a physical Android device to test the app.

## Basic Roadmap Skeleton
This roadmap outlines the development phases for Chirpie 2. We can update this as features are completed.

- [ ] **Phase 1: Project Initialization**
  - Initialize Git repo and push to GitHub.
  - Create the base Android Studio project (Empty Compose Activity).
  - Setup basic MVVM package structure (`data`, `ui`, `viewmodel`).
- [ ] **Phase 2: The Data Layer**
  - Create the Kotlin Data Class (`Bird.kt`) matching the multilingual text fields.
  - Set up Room Database for local data storage and querying.
  - Populate the database with initial bird data.
- [ ] **Phase 3: The UI Layer (Jetpack Compose)**
  - Build the **List Screen**: A scrolling `LazyColumn` displaying bird names and thumbnails.
  - Build the **Detail Screen**: A screen showing all translated names, a large image, and an audio play button.
  - Implement navigation between the List and Detail screens.
- [ ] **Phase 4: Multimedia Integration**
  - Integrate Coil to load bird images seamlessly.
  - Integrate `MediaPlayer` to handle playing bird calls/songs on the Detail Screen.
- [ ] **Phase 5: Polish & Deployment**
  - UI styling, theme implementation (Dark/Light mode support).
  - Performance profiling and bug fixing.
  - Generate signed APK/App Bundle for distribution.
  - 
