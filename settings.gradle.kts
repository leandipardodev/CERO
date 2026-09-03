pluginManagement {
    repositories {
        google {
            mavenContent {
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
    repositories {
        google {
            mavenContent {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "CERO-Remote-PC-Tools"

include(":app:android")
include(":core:connection")
include(":core:shared")
include(":feature:filetransfer")
include(":feature:joystick")
include(":feature:speaker")
include(":feature:steering")
include(":feature:mic")
