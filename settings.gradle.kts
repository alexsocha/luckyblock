pluginManagement {
    repositories {
        mavenCentral()
        maven {
            name = "Fabric"
            url = java.net.URI("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }

    plugins {
        val fabricLoomVersion: String by settings
        val kotlinVersion: String by settings

        kotlin("multiplatform") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("js") version kotlinVersion
        id("fabric-loom") version fabricLoomVersion
        id("com.palantir.git-version") version "0.12.3"
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    kotlin("js") apply false
}

rootProject.name = "luckyblock"
include("common")
include("fabric")
include("bedrock")
