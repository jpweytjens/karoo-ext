# Karoo Extension

## References

[Quick Start & Docs](https://hammerheadnav.github.io/karoo-ext/index.html)

[Jitpack Build](https://jitpack.io/#io.hammerhead/karoo-ext)

## Sample App

Within [app/](app/), a sample application with Karoo Extensions is provided with
examples for various integrations with Karoo.

Main activity demonstrates:
1. HW actions
2. Beeper control
3. Subscribing to system events

The functionality demonstrated in [MainActivity](app/src/main/kotlin/io/hammerhead/sampleext/MainActivity.kt) can
also be used in extensions to create more advanced in-ride behavior.

To install the basic sample app:
```bash
./gradlew app:installDebug
```

### Sample Extension

The sample app includes an [Extension](app/src/main/kotlin/io/hammerhead/sampleext/extension)
This is a good starting point or reference if you have an existing Android app written in Kotlin.

The sample extension demonstrates:
1. Defining the extension info in XML
2. Scanning for devices
3. Connecting to and updating a device
4. Defining a data type
5. Streaming data for a data type
6. Adding a custom view for a data type
