pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases")
    }

    plugins {
        kotlin("jvm") version "2.0.21"
    }
}

plugins {
    kotlin("jvm") apply false
}

rootProject.name = "lucky-block"
include("common")
include("tools")
include("neoforge")
//include("fabric")
//include("bedrock")
