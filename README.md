# Ludo Supreme - Authentic Indian Board Game

<p align="center">
  <img src="docs/logo.png" width="180" alt="Ludo Supreme Game Logo" style="border-radius: 36px;" />
</p>

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Ludo Supreme is an authentic, high-performance Indian board game built 100% with Jetpack Compose for Android. It features 2.5D tumbling dice physics, intelligent bot AI opponents, zero-asset procedural PCM sound synthesis, and fully adaptive layouts optimized for both tablets and phones in portrait and landscape orientations.

---

## Features

- **2.5D Tumbling Dice Physics**: Snappy two-phase tumble with 3D perspective transforms, rapid dynamic face cycling motion blur, abrupt impact flat snap, and overshoot settling.
- **Procedural 3D Map-Pin Pawns**: Signature Ludo King style tokens featuring glossy colored dome inserts, realistic drop shadows, elevated tip perspectives, and golden pulsing glow indicators.
- **Classic Authentic Indian Board**: Crisp 15x15 grid with colored corner yards, safe star cells, directional entrance arrows, and multi-colored center victory triangles.
- **Intelligent Bot Opponents**: Strategic bot AI with heuristic decision trees prioritizing winning moves, opponent token captures, yard exits on 6s, home stretch safety, and board progress.
- **Step-by-Step Hop Traversal**: Authentic cadence hop animations across outer tracks and colored home stretches with synchronized token step sound effects.
- **Smart Auto-Play**: Seamless auto-movement when only a single token is eligible to play or when rolling 6 to open a token from yard.
- **Procedural PCM Audio Synthesizer**: Low-latency audio engine generating real-time PCM waveforms via Android `AudioTrack` (dice shake & tumble, hop steps, captures, yard exits, victory fanfare) without any external MP3/WAV assets.
- **Adaptive Layout & Symmetry**: Seamlessly adapts between phone portrait and tablet landscape orientations with full auto-rotation handling and symmetric player pod arrangements.
- **Multiplayer & Bot Customization**: Play with 2, 3, or 4 players with configurable human and bot participants.

---

## Technical Stack

- **UI & Graphics**: 100% Jetpack Compose with custom hardware-accelerated Canvas and physics-driven Animatable transitions.
- **Architecture**: Clean MVVM architecture with Kotlin Coroutines, StateFlow, Job concurrency management, and a deterministic game engine (`LudoGameEngine`).
- **Audio Engine**: Zero-asset procedural PCM synthesizer (`SoundManager`) generating sine, square, and triangle waves directly on native Android `AudioTrack`.
- **Compatibility**: Android 8.0+ (API 26+) with compileSdk 35 and targetSdk 35.

---

## Quick Start

### Build and Run

```bash
# Clone the repository
git clone https://github.com/gitadityakumar/ludo-.git
cd ludo-

# Run unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug

# Install and launch on connected device
./gradlew installDebug
adb shell am start -n com.example.ludo/.MainActivity
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
