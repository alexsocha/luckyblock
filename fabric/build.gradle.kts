import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("fabric-loom")
}

val fabricModVersion: String by project
val fabricLatestMCVersion: String by project
val fabricMCTargetVersion: String by project
val fabricAPIVersion: String by project
val fabricMinLoaderVersion: String by project
val fabricMappingsVersion: String by project

base.archivesBaseName = rootProject.name
version = fabricModVersion

repositories {
    mavenCentral()
    maven (url = "http://maven.fabricmc.net/") {
        name = "Fabric"
    }
}

dependencies {
    compile(project(":common"))
    //compile(kotlin("stdlib-jdk8")) // using 'implementation' doesn't allow us to bundle this
    include(modImplementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = "1.5.0+kotlin.1.4.31"))
    minecraft("com.mojang:minecraft:$fabricLatestMCVersion")
    mappings("net.fabricmc:yarn:$fabricMappingsVersion:v2")
    modImplementation("net.fabricmc:fabric-loader:[$fabricMinLoaderVersion,)")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricAPIVersion")
}

tasks.processResources {
    from("../common/src/jvmMain/resources/game")
    inputs.property("fabricModVersion", fabricModVersion)
    filesMatching("fabric.mod.json") {
        expand(
            "fabricModVersion" to fabricModVersion,
            "fabricMCTargetVersion" to fabricMCTargetVersion,
            "fabricMinLoaderVersion" to fabricMinLoaderVersion
        )
    }
}

tasks.assemble { dependsOn(tasks.getByName("dist").mustRunAfter(tasks.remapJar)) }
tasks.getByName("runClient") { dependsOn(tasks.getByName("copyRunResources")) }
tasks.getByName("runServer") { dependsOn(tasks.getByName("copyRunResources")) }

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

tasks.withType<JavaCompile>().configureEach{
    this.options.encoding = "UTF-8"
    val targetVersion = 8
    if (JavaVersion.current().isJava9Compatible) {
        this.options.release.set(targetVersion)
    }
}

tasks.jar {
    archiveBaseName.set(rootProject.name)
}

tasks.getByName<Zip>("dist") {
    configurations.compile.get().filter { it.name.startsWith("kotlin-stdlib") || it.name.startsWith("common") }.forEach {
        from(zipTree(it))
    }
}
