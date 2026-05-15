pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Mini Video Journal"

include(":app")
include(":core-domain")
include(":core-data")
include(":core-database")
include(":core-testing")
include(":core-ui")
include(":feature-feed-navigation")
include(":feature-feed")
include(":feature-camera-navigation")
include(":feature-camera")
include(":test-app")
