import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Date
import org.ajoberstar.grgit.Grgit

plugins {
    kotlin("multiplatform")
    id("org.ajoberstar.grgit")
}

repositories {
    mavenCentral()
}

val isForgeEnabled: String by project
val isForgeEnabledBool = isForgeEnabled.toBoolean()
val forgeModVersion: String by project
val forgeMinForgeVersion: String by project
val forgeMinMCVersion: String by project

val isFabricEnabled: String by project
val isFabricEnabledBool = isFabricEnabled.toBoolean()
val fabricModVersion: String by project
val fabricMinMCVersion: String by project

val isBedrockEnabled: String by project
val isBedrockEnabledBool = isBedrockEnabled.toBoolean()
val bedrockModVersion: String by project
val bedrockTemplateAddonVersion: String by project

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js {
        browser {
            useCommonJs()
        }
    }

    sourceSets {
        val commonMain by getting

        if (isForgeEnabledBool || isFabricEnabledBool) {
            val jvmMain by getting
            val jvmTest by getting {
                dependencies {
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                    // todo: separate common and JVM tests when mockk adds multiplatform support
                    // implementation("io.mockk:mockk-common:1.+")
                    implementation(kotlin("test-junit"))
                    implementation("io.mockk:mockk:1.+")
                }
            }
        }

        if (isBedrockEnabledBool) {
            val jsMain by getting
        }
    }
}

tasks.getByName<ProcessResources>("jvmProcessResources") {
    exclude("*")
}

typealias TemplateAddonVersions = Map<String, List<Map<String, String>>>

@Suppress("UNCHECKED_CAST")
val javaTemplateAddonVersions = groovy.json.JsonSlurper().parseText(
    file("src/jvmMain/resources/template-addons/versions.json").readText()
) as TemplateAddonVersions

fun copyJvmRuntimeResources(projectName: String, taskName: String, loaderName: String, modVersion: String, dir: String) {
    val latestTemplateAddon = javaTemplateAddonVersions["versions"]!![0]["folder"]
    project(":$projectName").tasks.register<Copy>(taskName) {
        into(dir)
        into("config/lucky/$modVersion-$loaderName") {
            from("$rootDir/common/src/jvmMain/resources/lucky-config")
        }
        into("addons/lucky/lucky-block-custom") {
            from("$rootDir/common/src/jvmMain/resources/template-addons/${latestTemplateAddon}")
        }
    }
}
if (isForgeEnabledBool) copyJvmRuntimeResources(projectName="forge", taskName="copyRunResources", loaderName="forge", modVersion=forgeModVersion, dir="$rootDir/run")
if (isFabricEnabledBool) copyJvmRuntimeResources(projectName="fabric", taskName="copyRunResources", loaderName="fabric", modVersion=fabricModVersion, dir="$rootDir/run")

tasks.register<Copy>("jvmTestCopyRunResources") {
    into("build/test-run")
    into("config/lucky/0.0.0-0-test") {
        from("$rootDir/common/src/jvmMain/resources/lucky-config")
    }
    into("addons/lucky") {
        from("$rootDir/common/src/jvmMain/resources/template-addons")
    }
}
tasks.getByName("jvmTest").dependsOn(tasks.getByName("jvmTestCopyRunResources"))

tasks.register<Zip>("jvmConfigDist") {
    archiveFileName.set("lucky-config.zip")
    destinationDirectory.set(file("$rootDir/common/build/tmp"))
    from("src/jvmMain/resources/lucky-config")
}

fun getModVersionNumber(modVersion: String): Int {
    val splitVersion = modVersion.split('-')
    val mcVersion = splitVersion[0].split('.')
    return (mcVersion[0].toInt()) * 1000000 +
        (mcVersion[1].toInt()) * 10000 +
        (mcVersion[2].toInt()) * 100 +
        splitVersion[1].toInt()
}

fun writeMeta(distDir: String, version: String, versionNumber: Int, minMinecraftVersion: String, extraInfo: Map<String, String> = emptyMap()) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")

    val git = Grgit.open(mapOf("currentDir" to project.rootDir))
    val revision = git.head().abbreviatedId

    file(distDir).mkdirs()
    val meta = mapOf(
        "version" to version,
        "version_number" to versionNumber,
        "min_minecraft_version" to minMinecraftVersion,
        "revision" to revision,
        "datetime" to dateFormat.format(Date())
    ) + extraInfo
    file("$distDir/meta.yaml").writeText(
        meta.entries.joinToString("") { (k, v) -> "$k: $v\n" }
    )
}

fun writeTemplateAddonMeta(versionsJson: TemplateAddonVersions) {
    val latest = versionsJson["versions"]!![0]
    writeMeta(
        distDir = "$rootDir/dist/${latest["folder"]}",
        version = latest["version"]!!,
        versionNumber = latest["version"]!!.toInt(),
        minMinecraftVersion = latest["min_minecraft_version"]!!,
        extraInfo = mapOf("min_mod_version" to latest["min_mod_version"]!!)
    )
}

tasks.register<Zip>("jvmTemplateAddonDist") {
    val distName = javaTemplateAddonVersions["versions"]!![0]["folder"]
    doFirst { writeTemplateAddonMeta(javaTemplateAddonVersions) }

    archiveFileName.set("$distName.zip")
    destinationDirectory.set(file("$rootDir/dist/$distName"))
    from("src/jvmMain/resources/template-addons/$distName")
    from("dist/$distName/meta.yaml")
}

if (isBedrockEnabledBool) {
    project(":bedrock").tasks.register<Zip>("templateAddonDist") {
        // TODO
    }
}

fun jvmJarDist(projectName: String, minMCVersion: String, modVersion: String) {
    project(":$projectName").tasks.register<Zip>("jarDist") {
        val distName = "${rootProject.name}-$modVersion-$projectName"
        destinationDirectory.set(file("$rootDir/dist/$distName"))
        archiveFileName.set("$distName.jar")

        doFirst {
            writeMeta(
                distDir = "$rootDir/dist/$distName",
                version = modVersion,
                versionNumber = getModVersionNumber(modVersion),
                minMinecraftVersion = minMCVersion,
                extraInfo = if (projectName == "forge") mapOf("min_forge_version" to forgeMinForgeVersion) else emptyMap()
            )
        }

        from(zipTree("$rootDir/$projectName/build/libs/${rootProject.name}-$modVersion.jar"))
        from("$rootDir/common/build/tmp/lucky-config.zip") { into("mod/lucky/java") }
        from("$rootDir/dist/$distName/meta.yaml")

        dependsOn(tasks.getByName("jvmConfigDist"))
    }
}
if (isForgeEnabledBool) jvmJarDist("forge", forgeMinMCVersion, forgeModVersion)
if (isFabricEnabledBool) jvmJarDist("fabric", fabricMinMCVersion, fabricModVersion)
if (isBedrockEnabledBool) {
    project(":bedrock").tasks.register<Zip>("dist") {
        val distName = "${rootProject.name}-$bedrockModVersion-bedrock"
        destinationDirectory.set(file("$rootDir/dist/$distName"))
        archiveFileName.set("$distName.mcpack")

        doFirst {
            writeMeta(
                distDir = "$rootDir/dist/$distName",
                version = bedrockModVersion,
                versionNumber = getModVersionNumber(bedrockModVersion),
                minMinecraftVersion = bedrockModVersion.split('-')[0]
            )
        }

        from("$rootDir/bedrock/build/mcpack/$distName")
        //dependsOn(tasks.getByName("processResources")) // TODO
    }
}

tasks.register<Delete>("jvmCleanDist") {
    delete("$rootDir/dist")
}

tasks.clean { dependsOn(tasks.getByName("jvmCleanDist")) }

