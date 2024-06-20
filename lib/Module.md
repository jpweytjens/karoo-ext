# Module karoo-ext

Karoo Extensions can easily be added to an existing Android application. If you
don't already have an Android application, create a new Android project now.

### Dependency Setup

Add [Github package](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package) repository to your buildscript in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    // ...
    repositories {
        // ...
        // karoo-ext from Github Packages
        maven {
            url = uri("https://maven.pkg.github.com/hammerheadnav/karoo-ext")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("USERNAME"))
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("TOKEN"))
            }
        }
    }
}
```

Add `karoo-ext` dependency:

```
implementation("io.hammerhead:karoo-ext:1.0.3")
```

### Karoo System Service

Karoo System Service can be included in any Android Activity or Service with:

```kotlin
private val karooSystem = KarooSystemService(this)
```

`karooSystem` exposes the ability dispatch effects to the system or consume events.

#### Karoo System Events

Add a consumer with:

```kotlin
val consumerId = karooSystem.addConsumer { rideState: RideState ->
    println("Ride state is now $rideState")
}
```

Be sure to clean up with:

```kotlin
karooSystem.removeConsumer(consumerId)
```

#### Dispatching Karoo System Effects

Send effects to the system with:

```kotlin
karooSystem.dispatch(PerformHardwareAction.ControlCenterComboPress)
```

### Extension Implementation

An extension service is required for the Karoo System to communicate with your application. This is not
necessary for implementations that want to interact with Karoo by receiving events or performing actions.

#### Extension

To enable your extension, create an Android service derived from `KarooExtension`.

##### Registration

In order to register your extension with the Karoo System,
the following meta data should be added to your `AndroidManifest.xml`
within the `<service></service>`.

Extension intent identifies this service as a Karoo Extension:

```xml

<intent-filter>
    <action android:name="io.hammerhead.karooext.KAROO_EXTENSION" />
</intent-filter>
```

Extension info provides an XML resource defining the extension's capabilities.

```xml

<meta-data android:name="io.hammerhead.karooext.EXTENSION_INFO" android:resource="@xml/extension_info" />
```

Extension Info XML resource (`extension_info.xml`):

```xml
<?xml version="1.0" encoding="utf-8"?>
<ExtensionInfo displayName="@string/extension_name" icon="@drawable/X" id="<extension-id>" scansDevices="true">
    <DataType description="@string/X" displayName="@string/X" graphical="true" icon="@drawable/X" typeId="<type id>" />
    ...
    <DataType... />
</ExtensionInfo>
```
