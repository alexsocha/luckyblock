import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.userdev.UserDevExtension
import mod.lucky.build.*

val rootProjectProps = RootProjectProperties.fromProjectYaml(rootProject.rootDir)
val projectProps = rootProjectProps.projects[ProjectName.LUCKY_BLOCK_FORGE]!!

buildscript {
    repositories {
        maven("https://maven.minecraftforge.net")
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1.46")
    }
}

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("mod.lucky.build.JavaEditionTasks")
}

apply {
    plugin("net.minecraftforge.gradle")
}

dependencies {
    compileOnly(project(":tools"))
    implementation(kotlin("stdlib-jdk8"))
    "minecraft"("net.minecraftforge:forge:${projectProps.lockedDependencies["minecraft-forge"]!!}")
    shadow(project(":common"))
}

repositories {
    mavenCentral()
}

group = "mod.lucky.forge"
base.archivesBaseName = rootProject.name
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

configure<UserDevExtension> {
    mappings("official", projectProps.devDependencies["forge-mappings"]!!.toGradleRange())

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
    inputs.property("modVersion", projectProps.version)
    filesMatching("META-INF/mods.toml") {
        expand(
            "modVersion" to projectProps.version,
            "minMinecraftVersion" to projectProps.dependencies["minecraft"]!!.minInclusive,
            "minForgeVersion" to projectProps.dependencies["forge"]!!.minInclusive,
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
    tasks.getByName("prepareRuns").dependsOn(tasks.getByName("copyRuntimeResources"))
    tasks.getByName("prepareRuns").dependsOn(tasks.getByName("copyRuntimeClasses"))

    tasks.getByName("reobfJar").dependsOn(tasks.getByName("copyShadowJar"))
    tasks.assemble {
        dependsOn(tasks.getByName("exportDist").mustRunAfter(tasks.getByName("reobfJar")))
    }
    tasks.clean {
        dependsOn(tasks.getByName("cleanDist"))
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
