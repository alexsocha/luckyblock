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
        id("org.ajoberstar.grgit") version "4.1.0"
        id("com.github.johnrengelman.shadow") version "7.0.0"
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
