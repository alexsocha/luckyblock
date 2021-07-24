import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    kotlin("jvm")
    id("fabric-loom")
    id("com.github.johnrengelman.shadow")
}

val fabricModVersion: String by project
val fabricLatestMCVersion: String by project
val fabricMinMCVersion: String by project
val fabricAPIVersion: String by project
val fabricMinLoaderVersion: String by project
val fabricMappingsVersion: String by project

base.archivesBaseName = rootProject.name
version = fabricModVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib-jdk8"))
    minecraft("com.mojang:minecraft:$fabricLatestMCVersion")
    mappings("net.fabricmc:yarn:$fabricMappingsVersion:v2")
    modImplementation("net.fabricmc:fabric-loader:[$fabricMinLoaderVersion,)")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricAPIVersion")

    shadow(project(":common"))
}

tasks.processResources {
    from("../common/src/jvmMain/resources/game")
    inputs.property("fabricModVersion", fabricModVersion)
    filesMatching("fabric.mod.json") {
        expand(
            "fabricModVersion" to fabricModVersion,
            "fabricMinMCVersion" to fabricMinMCVersion,
            "fabricMinLoaderVersion" to fabricMinLoaderVersion
        )
    }
}

tasks.assemble { dependsOn(tasks.getByName("jarDist").mustRunAfter(tasks.remapJar)) }
tasks.getByName("runClient").dependsOn(tasks.getByName("copyRunResources"))
tasks.getByName("runServer").dependsOn(tasks.getByName("copyRunResources"))

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "16"

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(16)
}

tasks.jar {
    archiveBaseName.set(rootProject.name)
}

val shadowJar by tasks.getting(ShadowJar::class) {
    configurations = listOf(project.configurations.shadow.get())
    relocate("kotlin", "mod.lucky.kotlin")
    relocate("org.jetbrains", "mod.lucky.jetbrains")
    minimize()
}

tasks.getByName<RemapJarTask>("remapJar") {
    input.set(shadowJar.archiveFile.get())
    dependsOn(tasks.getByName("shadowJar"))
}

tasks {
    val javaVersion = JavaVersion.VERSION_16
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}
