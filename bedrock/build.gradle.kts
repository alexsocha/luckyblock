import java.io.File
import java.util.UUID
import mod.lucky.build.*

val rootProjectProps = RootProjectProperties.fromProjectYaml(rootProject.rootDir)

repositories {
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    kotlin("js")
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":common"))
}

buildscript {
    dependencies {
        classpath(files("$rootDir/tools/build/classes"))
    }
}

kotlin {
    js {
        browser {
            dceTask {
                keep("luckyblock-bedrock.mod.lucky.bedrock.initServer")
            }
            webpackTask {
                outputFileName = "compiledServerScript.js"
                devtool = "hidden-source-map"
            }
        }
    }
}

val nodeVersion: String by project

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = nodeVersion
}

tasks.register<JavaExec>("generateLuckyBlockAddon") {
    doFirst {
        delete("./build/lucky-block-addon")
    }
    classpath = fileTree("$rootDir/tools/build/install/tools/lib")
    mainClass.set("mod.lucky.tools.MainKt")
    args = listOf(
        "generate-bedrock-addon",
        "--inputConfigFolder",
        "./lucky-block-config",
        "--outputAddonFolder",
        "./build/lucky-block-addon",
    )
    dependsOn(project(":tools").tasks.getByName("installDist"))
}

tasks.register<JavaExec>("generateCustomLuckyBlockAddon") {
    doFirst {
        delete("./build/custom-lucky-block-addon")
    }
    classpath = fileTree("$rootDir/tools/build/install/tools/lib")
    mainClass.set("mod.lucky.tools.MainKt")
    args = listOf(
        "generate-bedrock-addon",
        "--inputConfigFolder",
        "./template-addon",
        "--outputAddonFolder",
        "./build/custom-lucky-block-addon",
    )
    dependsOn(project(":tools").tasks.getByName("installDist"))
}

tasks.register<Sync>("copyRuntimeAddons") {
    val addonPaths = mapOf(
        "LuckyBlock" to "./build/lucky-block-addon",
        "CustomLuckyBlock" to "./build/custom-lucky-block-addon",
    )

    for ((addonName, addonPath) in addonPaths) {
        from("$addonPath/resource_pack") {
            into("development_resource_packs/${addonName}RP")
        }
        from("$addonPath/behavior_pack") {
            into("development_behavior_packs/${addonName}BP")
        }
    }

    into("./run")
    dependsOn("copyCompiledServerScript")
    dependsOn("generateBedrockDrops")
    dependsOn("nbtToMcstructure")

    // workaround: gradle doesn't always detect that the input folder has changed
    outputs.upToDateWhen { false }
}

tasks.register<Copy>("copyCompiledServerScript") {
    from("./build/distributions/compiledServerScript.js") {
        rename("compiledServerScript.js", "serverScript.js")
    }
    into("$addonDistDir/behavior_pack/scripts/server")

    dependsOn("generateBedrockDrops")
}

tasks.register<Zip>("exportLuckyBlockAddon") {
    val version = rootProjectProps.projects[ProjectName.LUCKY_BLOCK_BEDROCK]!!.version
    val distName = "${ProjectName.LUCKY_BLOCK_BEDROCK.fullName}-${version}"
    val distDir = file("$rootDir/dist/$distName")
    destinationDirectory.set(file("$rootDir/dist/$distName"))
    archiveFileName.set("$distName.mcpack")

    doFirst {
        val distMeta = rootProjectProps.getDistMeta(rootDir, ProjectName.LUCKY_BLOCK_BEDROCK)
        file(distDir).mkdirs()
        file("$distDir/meta.yaml").writeText(distMeta.toYaml())
    }
    from("$rootDir/bedrock/build/processedResources/js/main/pack")
}

tasks.named("build").configure {
    tasks.getByName("browserProductionWebpack").dependsOn("generateLuckyBlockAddon")
    tasks.getByName("browserProductionWebpack").dependsOn("generateCustomLuckyBlockAddon")
    tasks.getByName("browserProductionWebpack").inputs.file("./build/processedResources/serverScript.js")
    tasks.getByName("copyCompiledServerScript").dependsOn("browserProductionWebpack")
    tasks.getByName("exportDist").dependsOn("browserProductionWebpack")
    dependsOn("browserProductionWebpack")
    dependsOn("copyCompiledServerScript")
    dependsOn("exportLuckyBlockAddon")
    dependsOn("copyRuntimeAddons")
}

tasks.register("buildDev").configure {
    tasks.getByName("browserDevelopmentWebpack").dependsOn("generateLuckyBlockAddon")
    tasks.getByName("browserDevelopmentWebpack").dependsOn("generateCustomLuckyBlockAddon")
    tasks.getByName("browserDevelopmentWebpack").inputs.file("./build/processedResources/generated-config.js")
    tasks.getByName("copyCompiledServerScript").dependsOn("browserDevelopmentWebpack")
    dependsOn("browserDevelopmentWebpack")
    dependsOn("copyCompiledServerScript")
    dependsOn("copyRuntimePacks")
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.LENIENT)
}
