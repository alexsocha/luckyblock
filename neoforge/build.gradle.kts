import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import mod.lucky.build.*

val rootProjectProps = RootProjectProperties.fromProjectYaml(rootProject.rootDir)
val projectProps = rootProjectProps.projects[ProjectName.LUCKY_BLOCK_FORGE]!!

buildscript {
    repositories {
        maven("https://maven.neoforged.net/releases")
    }
}

plugins {
    kotlin("jvm")
    id("mod.lucky.build.JavaEditionTasks")
    id("net.neoforged.moddev") version "1.0.21"
    //id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencies {
    compileOnly(project(":tools"))
    implementation(project(":common"))
    implementation("thedarkcolour:kotlinforforge-neoforge:${projectProps.lockedDependencies["kotlinforforge"]}")
}

repositories {
    mavenLocal()
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

group = "mod.lucky.forge"
base.archivesName = rootProject.name
version = projectProps.version

neoForge {
    version = projectProps.lockedDependencies["neoforge"]!!

    parchment {
        minecraftVersion = projectProps.lockedDependencies["parchment-minecraft"]!!
        mappingsVersion = projectProps.lockedDependencies["parchment-mappings"]!!
    }

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", "lucky")
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", "lucky")
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create("lucky") {
            sourceSet(sourceSets["main"])
        }
    }
}

// sourceSets.main.resources.srcDir("src/generated/resources")

tasks.named<ProcessResources>("processResources").configure {
    from("../common/src/jvmMain/resources/game")
    inputs.property("modVersion", projectProps.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "modVersion" to projectProps.version,
            "minMinecraftVersion" to projectProps.dependencies["minecraft"]!!.minInclusive,
            "minNeoforgeVersion" to projectProps.dependencies["neoforge"]!!.minInclusive,
            "minLoaderVersion" to projectProps.dependencies["loader"]!!.minInclusive,
        )
    }
}

tasks.jar {
    archiveBaseName.set(rootProject.name)
}

val javaVersion = projectProps.dependencies["java"]!!.maxInclusive!!
java.toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = javaVersion

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion.toInt())
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.LENIENT)
}

// Activate reproducible builds
// https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
/*
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
 */
