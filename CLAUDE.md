# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **Barberfish** - a Karoo Extensions project inspired by the symbiotic relationship between barberfish and sharks. Like barberfish that clean parasites and dead skin from sharks while receiving protection and food in return, this extension removes data parasites and adds useful bits to your Hammerhead Karoo.

The project consists of:
- `lib/`: The karoo-ext library module (unchanged from upstream) with AIDL interfaces and Kotlin APIs  
- `app/`: The Barberfish extension that provides configurable data fields

The Barberfish extension demonstrates a production-ready Karoo extension with enhanced data fields including a true average speed calculator (Randonneur) and a configurable triple data field with native Karoo integration.

## Development Commands

### Build Commands
```bash
# Build the entire project
./gradlew build

# Build and install the Barberfish app
./gradlew app:installDebug

# Build the library module only
./gradlew lib:build

# Clean build
./gradlew clean
```

### Code Quality
```bash
# Run code formatting (Spotless with ktlint)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck
```

### Testing
```bash
# Run all tests
./gradlew test

# Run library tests only
./gradlew lib:test
```

### Documentation
```bash
# Generate Dokka documentation
./gradlew dokkaHtml
```

## Architecture

### Core Components

**Library Module (`lib/`)**:
- **AIDL Interfaces** (`src/main/aidl/`): Defines IPC contracts between extensions and Karoo OS
  - `IKarooExtension.aidl`: Main extension interface
  - `IKarooSystem.aidl`: System service interface  
  - `IHandler.aidl`: Event handling interface
- **Models** (`models/`): Data classes for events, effects, device info, and system state
- **Extension Framework** (`extension/`): Base classes for creating extensions and data types
- **System Service** (`KarooSystemService.kt`): Main API for connecting to Karoo OS

**Barberfish App (`app/`)**:
- **MainActivity.kt**: Jetpack Compose configuration UI for the Barberfish extension
- **Extension Package** (`extension/`): Barberfish extension implementation
  - BarberfishExtension: Main extension class
  - Randonneur: True average speed data field including paused time
  - TripleDataField: Enhanced three-column data field with native Karoo integration

### Key Patterns

- **Event-Driven Architecture**: Extensions consume `KarooEvent`s and dispatch `KarooEffect`s
- **Process Isolation**: Extensions run in separate processes from Karoo OS for stability
- **AIDL Communication**: All system interactions use Android's AIDL for IPC
- **RemoteViews**: Custom UI components use Android's RemoteViews for cross-process rendering

### Extension Development Flow

1. Extend `KarooExtension` class with unique ID and version
2. Define extension capabilities in `extension_info.xml`
3. Register extension service in `AndroidManifest.xml`
4. Implement data types by extending `DataTypeImpl`
5. Use `KarooSystemService` to connect and interact with Karoo OS

### Dependencies

- **Android**: API 23+ (Android 6.0+), targets API 34
- **Kotlin**: 2.0.0 with coroutines support
- **Jetpack Compose**: For modern Android UI
- **Hilt**: Dependency injection framework
- **Glance**: For extension widget views
- **kotlinx-serialization**: For data serialization

## Code Style

The project uses Spotless with ktlint for code formatting:
- Spaces for indentation (no tabs)
- No line length limit
- Disabled value argument/parameter comments
- Special handling for Composable function naming

Always run `./gradlew spotlessApply` before committing changes.