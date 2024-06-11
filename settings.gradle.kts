pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // For local publishes of karoo-ext
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "karoo-ext"
include(":lib", ":app")
 