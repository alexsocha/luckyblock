import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import mod.lucky.build.*

val rootProjectProps = RootProjectProperties.fromProjectYaml(rootProject.rootDir)
val projectProps = rootProjectProps.projects[ProjectName.LUCKY_BLOCK_FABRIC]!!

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    // https://maven.fabricmc.net/net/fabricmc/fabric-loom/
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("mod.lucky.build.JavaEditionTasks")
    id("fabric-loom") version "[0.11.0,0.12)"
}

base.archivesBaseName = rootProject.name
version = projectProps.version

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib-jdk8"))
    minecraft("com.mojang:minecraft:${projectProps.dependencies["minecraft"]!!.toGradleRange()}")
    mappings("net.fabricmc:yarn:${projectProps.devDependencies["fabric-mappings"]!!.toGradleRange()}:v2")
    modImplementation("net.fabricmc:fabric-loader:${projectProps.dependencies["fabric-loader"]!!.toGradleRange()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${projectProps.dependencies["fabric-api"]!!.toGradleRange()}")
    shadow(project(":common"))
}

tasks.processResources {
    from("../common/src/jvmMain/resources/game")
    inputs.property("modVersion", projectProps.version)
    filesMatching("fabric.mod.json") {
        expand(
            "modVersion" to projectProps.version,
            "minMinecraftVersion" to projectProps.dependencies["minecraft"]!!.minInclusive,
            "minFabricLoaderVersion" to projectProps.dependencies["fabric-loader"]!!.minInclusive,
        )
    }
}

tasks.getByName("prepareRemapJar").dependsOn(tasks.getByName("shadowJar"))
tasks.assemble { dependsOn(tasks.getByName("exportDist").mustRunAfter(tasks.remapJar)) }
tasks.getByName("runClient").dependsOn(tasks.getByName("copyRuntimeResources"))
tasks.getByName("runServer").dependsOn(tasks.getByName("copyRuntimeResources"))

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
