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
        maven { url = uri("https://storage.googleapis.com/download.flutter.io") }
        maven {
            url = uri("https://storage.flutter-io.cn/download.flutter.io")
        }
        // Local Flutter module repository
        maven {
            url = uri("./.flutter_sdk/build/host/outputs/repo")
        }
    }
}

rootProject.name = "tradeable-android-wrapper"
include(":tradeable-sdk")
// Flutter module configuration - commented out until properly set up as Android module
// include(":flutter-module")
// project(":flutter-module").projectDir = File(rootDir, ".flutter_sdk")
