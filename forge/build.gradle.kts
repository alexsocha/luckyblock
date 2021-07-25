import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.userdev.UserDevExtension

buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net") }
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1.+") {
            isChanging=true
        }
    }
}

val forgeModVersion: String by project
val forgeLatestForgeVersion: String by project
val forgeLatestMCVersion: String by project
val forgeMinMCVersion: String by project
val forgeMinLoaderVersion: String by project
val forgeMappingsVersion: String by project


plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

apply {
    plugin("net.minecraftforge.gradle")
}

group = "mod.lucky.forge"
base.archivesBaseName = rootProject.name
version = forgeModVersion

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib-jdk8"))
    "minecraft"("net.minecraftforge:forge:$forgeLatestForgeVersion")
    shadow(project(":common"))
}

configure<UserDevExtension> {
    mappings("official", forgeMappingsVersion)

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs {
        create("client") {
            workingDirectory(project.file("../run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "info")
        }

        create("server") {
            workingDirectory(project.file("../run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "info")
        }
    }
}


tasks.processResources {
    from("../common/src/jvmMain/resources/game")
    inputs.property("forgeModVersion", forgeModVersion)
    filesMatching("META-INF/mods.toml") {
        expand(
            "forgeModVersion" to forgeModVersion,
            "forgeMinMCVersion" to forgeMinMCVersion,
            "forgeMinLoaderVersion" to forgeMinLoaderVersion
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
    tasks.getByName("prepareRuns").dependsOn(tasks.getByName("copyRunResources"))

    tasks.getByName("reobfJar").dependsOn(tasks.getByName("copyShadowJar"))
    tasks.assemble {
        dependsOn(tasks.getByName("jarDist").mustRunAfter(tasks.getByName("reobfJar")))
        dependsOn(project(":common").tasks.getByName("jvmTemplateAddonDist"))
    }
}


java.toolchain.languageVersion.set(JavaLanguageVersion.of(16))

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "16"

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(16)
}
