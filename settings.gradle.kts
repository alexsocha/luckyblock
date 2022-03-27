pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }

    plugins {
        kotlin("multiplatform") version "[1.6.0,1.7)"
        kotlin("jvm") version "[1.6.0,1.7)"
        kotlin("js") version "[1.6.0,1.7)"
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    kotlin("js") apply false
}

rootProject.name = "lucky-block"
include("common")
include("tools")
include("forge")
include("fabric")
include("bedrock")
