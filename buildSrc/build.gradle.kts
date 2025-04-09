plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "1.4.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.ajoberstar.grgit:grgit-core:5.3.0")
    implementation("com.charleskorn.kaml:kaml:0.37.0")
}
