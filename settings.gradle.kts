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
    }
}

rootProject.name = "Murajaah"
include(":app")
include(":core:fsrs")
include(":core:quran")
include(":core:scheduler")
include(":core:database")
include(":core:network")
include(":core:mushaf")
include(":feature:session")
include(":feature:auth")
include(":feature:onboarding")
