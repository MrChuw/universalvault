pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.postprocess.jsonlang") version "2.1-beta.4" apply false
}

stonecutter {
    create(rootProject) {
        versions("1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
        vcsVersion = "26.3"
    }
}

rootProject.name = "Universal Vault"