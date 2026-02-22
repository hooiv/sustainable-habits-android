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
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "sustainable-habits-android"
include(":app")
include(":benchmark")
include(":core:data")
include(":core:domain")
include(":core:network")
include(":core:ui")
include(":features:habits")
include(":features:ai")
include(":features:analytics")
include(":features:gamification")
include(":features:stats")
include(":features:auth")
include(":features:onboarding")
include(":features:settings")
include(":features:splash")
