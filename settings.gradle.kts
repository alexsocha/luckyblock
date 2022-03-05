pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }

    plugins {
        kotlin("multiplatform") version "[1.6.0,2.0.0)"
        kotlin("jvm") version "[1.6.0,2.0.0)"
        kotlin("js") version "[1.6.0,2.0.0)"
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
