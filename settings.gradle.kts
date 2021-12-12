pluginManagement {
    repositories {
        maven {
            name = "Forge"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        val fabricLoomVersion: String by settings
        val kotlinVersion: String by settings
        val grGitVersion: String by settings
        val shadowJarVersion: String by settings

        kotlin("multiplatform") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("js") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("fabric-loom") version fabricLoomVersion
        id("org.ajoberstar.grgit") version grGitVersion
        id("com.github.johnrengelman.shadow") version shadowJarVersion
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    kotlin("js") apply false
}

val isFabricEnabled: String by settings
val isFabricEnabledBool = isFabricEnabled.toBoolean()
val isForgeEnabled: String by settings
val isForgeEnabledBool = isForgeEnabled.toBoolean()
val isBedrockEnabled: String by settings
val isBedrockEnabledBool = isBedrockEnabled.toBoolean()

rootProject.name = "luckyblock"
include("common")
include("tools")
if (isForgeEnabledBool) include("forge")
if (isFabricEnabledBool) include("fabric")
if (isBedrockEnabledBool) include("bedrock")
