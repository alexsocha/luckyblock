import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.io.File

plugins {
    kotlin("js")
    id("com.moowork.node") version "1.3.1"
}

repositories {
    mavenCentral()
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
                outputFileName = "serverScript.js"
                devtool = "hidden-source-map"
            }
        }
    }
}

val addonDistDir = "$rootDir/bedrock/build/processedResources/js/main/addon"

tasks.named<ProcessResources>("processResources") {
    from("../common/src/jvmMain/resources/game/assets/lucky/textures/blocks") {
        into("addon/resource_pack/textures/blocks")
    }
    from("../common/src/jvmMain/resources/game/assets/lucky/textures/blocks") {
        into("addon/resource_pack")
        include("lucky_block.png")
        rename("lucky_block.png", "pack_icon.png")
    }
    from("../common/src/jvmMain/resources/game/assets/lucky/textures/blocks") {
        into("addon/behavior_pack")
        include("lucky_block.png")
        rename("lucky_block.png", "pack_icon.png")
    }
}

tasks.register<Delete>("clearStructures") {
    delete(File("$addonDistDir/behavior_pack/structures/lucky").walkTopDown().toList())
}

tasks.register<JavaExec>("generateBedrockDrops") {
    classpath = fileTree("$rootDir/tools/build/install/tools/lib")
    mainClass.set("mod.lucky.tools.MainKt")
    val outputJSFile = "$rootDir/bedrock/build/processedResources/generated-config.js"
    args = listOf(
        "generate-bedrock-drops",
        "$rootDir/bedrock/src/main/resources/lucky-config",
        "--blockId",
        "lucky_block",
        "--outputJSFile",
        outputJSFile,
        "--outputStructuresFolder",
        "$addonDistDir/behavior_pack/structures/lucky",
    )

    doLast {
        // allow serverSystem to be imported from the generated JS file,
        // so that we can configure it further
        File(outputJSFile).appendText("\n\nmodule.exports = { \"serverSystem\": serverSystem }\n")
    }

    dependsOn(project(":tools").tasks.getByName("installDist"))
}

tasks.register<JavaExec>("nbtToMcstructure") {
    classpath = fileTree("$rootDir/tools/build/install/tools/lib")
    mainClass.set("mod.lucky.tools.MainKt")
    args = listOf(
        "nbt-to-mcstructure",
        "$rootDir/common/src/jvmMain/resources/lucky-config/structures",
        "--outputStructuresFolder",
        "$addonDistDir/behavior_pack/structures/lucky",
        "--blockConversionFile",
        "$rootDir/tools/block-conversion.yaml",
        "--outputGeneratedBlockConversionFile",
        "$rootDir/tools/.debug/block-conversion.generated.yaml",
    )
    dependsOn(project(":tools").tasks.getByName("installDist"))
}

tasks.register<Sync>("copyRuntimePacks") {
    val addonPaths = mapOf(
        "LuckyBlock" to "./build/processedResources/js/main/addon",
        "LuckyBlockCustom" to "./build/processedResources/js/main/template-addons/template-addon-1-bedrock",
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
    dependsOn("copyServerScript")
    dependsOn("clearStructures")
    dependsOn("generateBedrockDrops")
    dependsOn("nbtToMcstructure")

    // workaround: gradle doesn't always detect that the input folder has changed
    outputs.upToDateWhen { false }
}

tasks.register<Copy>("copyServerScript") {
    from("./build/distributions/serverScript.js")
    into("$addonDistDir/behavior_pack/scripts/server")

    dependsOn("generateBedrockDrops")
}

tasks.register<Zip>("zipPack") {
    archiveFileName.set("pack.zip")
    destinationDirectory.set(file("./dist"))
    from("./build/main/resources/pack")

    dependsOn("processResources")
}


tasks.named("build").configure {
    tasks.getByName("browserProductionWebpack").dependsOn("generateBedrockDrops")
    tasks.getByName("browserProductionWebpack").inputs.file("./build/processedResources/generated-config.js")
    tasks.getByName("copyServerScript").dependsOn("browserProductionWebpack")
    tasks.getByName("dist").dependsOn("browserProductionWebpack")
    dependsOn("browserProductionWebpack")
    dependsOn("copyServerScript")
    dependsOn("dist")
    dependsOn("copyRuntimePacks")
}

tasks.register("buildDev").configure {
    tasks.getByName("browserDevelopmentWebpack").dependsOn("generateBedrockDrops")
    tasks.getByName("browserDevelopmentWebpack").inputs.file("./build/processedResources/generated-config.js")
    tasks.getByName("copyServerScript").dependsOn("browserDevelopmentWebpack")
    dependsOn("browserDevelopmentWebpack")
    dependsOn("copyServerScript")
    dependsOn("copyRuntimePacks")
}
