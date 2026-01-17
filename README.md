# VeilCore Hytale Plugin

A professional Hytale plugin built with Gradle.

## Project Structure

```
VeilCore/
├── src/main/java/com/veilcore/
│   ├── VeilCorePlugin.java       # Main plugin class
│   ├── commands/                  # Command implementations
│   ├── events/                    # Event listeners
│   ├── utils/                     # Utility classes
│   ├── config/                    # Configuration management
│   └── managers/                  # Manager classes
├── src/main/resources/
│   └── manifest.json              # Plugin manifest
├── build.gradle.kts               # Gradle build configuration
├── settings.gradle.kts            # Gradle settings
└── gradle.properties              # Plugin metadata
```

## Building

To build the plugin, run:
```bash
./gradlew build
```

The compiled JAR will be located in `build/libs/`.

## Installation

Copy the built JAR file to your Hytale server's `mods` folder.

## Version

1.0.0
