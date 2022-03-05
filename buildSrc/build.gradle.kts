plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.4.0"
}

dependencies {
    implementation("org.ajoberstar.grgit:grgit-core:4.1.0")
    implementation("com.charleskorn.kaml:kaml:0.37.0")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}