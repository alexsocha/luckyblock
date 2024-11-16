plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.9.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.ajoberstar.grgit:grgit-core:4.1.1")
    implementation("com.charleskorn.kaml:kaml:0.37.0")
}
