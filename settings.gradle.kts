pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Space Keyboard"

include(":app")
include(":ime")
include(":core")
include(":designsystem")
include(":feature-symbols")
include(":feature-emoji")
include(":feature-decorate")
include(":feature-clipboard")
include(":feature-translate")
include(":feature-autofix")
include(":feature-images")
