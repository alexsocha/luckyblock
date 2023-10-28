pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }

    plugins {
        kotlin("multiplatform") version "1.8.10"
        kotlin("jvm") version "1.8.10"
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
}

rootProject.name = "lucky-block"
include("common")
include("tools")
include("forge")
include("fabric")
//include("bedrock")
