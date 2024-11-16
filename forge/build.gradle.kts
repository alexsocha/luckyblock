import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("mod.lucky.build.JavaEditionTasks")
    id("net.neoforged.moddev") version "1.0.21"
}

dependencies {
    compileOnly(project(":tools"))
    implementation(kotlin("stdlib-jdk8"))
    shadow(project(":common"))
}

repositories {
    mavenLocal()
}

group = "mod.lucky.forge"
base.archivesName = rootProject.name
version = projectProps.version

tasks.register<Copy>("copyRuntimeClasses") {
    // since Forge mods are loaded as independent modules, we need to copy all runtime dependency
    // classes to the build/classes folder
    configurations.shadow.get().files.forEach {
        // ignore compile-only annotations
        if (!it.name.startsWith("annotations-")) {
            from(zipTree(it))
        }
    }
    into("build/classes/kotlin/main/")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    dependsOn("classes")
}

neoForge {
    // Specify the version of NeoForge to use.
    version = "21.1.73"

    parchment {
        mappingsVersion = "2024.07.28"
        minecraftVersion = "1.21"
    }

    // Default run configurations.
    runs {
        create("client") {
            client()
            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("neoforge.enabledGameTestNamespaces", "lucky")
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", "lucky")
        }

        // Applies to all the run configs above
        configureEach {
            // Recommended logging data for a userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            systemProperty("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        // Define mod <-> source bindings
        create("lucky") {
            sourceSet(sourceSets["main"])
        }
    }
}

// sourceSets.main.resources.srcDir("src/generated/resources")

tasks.processResources {
    from("../common/src/jvmMain/resources/game")
    inputs.property("modVersion", projectProps.version)
    filesMatching("META-INF/mods.toml") {
        expand(
            "modVersion" to projectProps.version,
            "minMinecraftVersion" to projectProps.dependencies["minecraft"]!!.minInclusive,
            "minForgeVersion" to projectProps.dependencies["forge"]!!.minInclusive,
            // FML version == Forge major version
            "minFMLVersion" to projectProps.dependencies["forge"]!!.minInclusive!!.split('.')[0],
        )
    }
}

tasks.jar {
    archiveBaseName.set(rootProject.name)
}

val shadowJar by tasks.getting(ShadowJar::class) {
    configurations = listOf(project.configurations.shadow.get())
    relocate("kotlin", "mod.lucky.kotlin")
    relocate("org.jetbrains", "mod.lucky.jetbrains")
    minimize()

    dependsOn(tasks.getByName("jar"))
}

tasks.register<Copy>("copyShadowJar") {
    from(tasks.jar.get().destinationDirectory.get())
    into(tasks.jar.get().destinationDirectory.get())
    include(shadowJar.archiveFile.get().asFile.name)
    rename(shadowJar.archiveFile.get().asFile.name, tasks.jar.get().archiveFileName.get())

    dependsOn(tasks.getByName("shadowJar"))
}

afterEvaluate {
    tasks.getByName("jar").dependsOn(tasks.getByName("copyRuntimeClasses"))
    //tasks.getByName("prepareRuns").dependsOn(tasks.getByName("copyRuntimeClasses"))
    //tasks.getByName("prepareRuns").dependsOn(tasks.getByName("copyRuntimeResources"))

    //tasks.getByName("reobfJar").dependsOn(tasks.getByName("copyShadowJar"))

    tasks.assemble {
        //dependsOn(tasks.getByName("exportDist").mustRunAfter(tasks.getByName("reobfJar")))
    }
    tasks.clean {
        //dependsOn(tasks.getByName("cleanDist"))
    }
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
