import com.moowork.gradle.node.task.NodeTask

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
            useCommonJs()
            dceTask {
                keep("luckyblock-bedrock.mod.lucky.bedrock.initServer")
            }
        }
    }
}

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

task<JavaExec>("generateConfig") {
    classpath = fileTree("$rootDir/tools/build/install/tools/lib")
    main = "mod.lucky.tools.GenerateBedrockConfigKt"
    args = listOf(
        "$rootDir/bedrock/src/main/resources/addon-config",
        "--blockId",
        "lucky_block",
        "--outputJSFile",
        "$rootDir/bedrock/build/processedResources/generated-config.js",
        "--outputStructuresFolder",
        "$rootDir/bedrock/build/processedResources/generated-structures",
    )
}

task<NodeTask>("webpack") {
    setScript(File("$rootDir/build/js/node_modules/webpack/bin/webpack"))
    dependsOn("browserProductionWebpack")
    dependsOn("generateConfig")
}

task<Copy>("copyRuntimePacks") {
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

    dependsOn("assemble")
}

task<Zip>("zipPack") {
    archiveFileName.set("pack.zip")
    destinationDirectory.set(file("./dist"))
    from("./build/main/resources/pack")

    dependsOn("processResources")
}

tasks.named("assemble").configure {
    tasks.getByName("generateConfig").dependsOn(project(":tools").tasks.getByName("installDist"))
    dependsOn("webpack")
    dependsOn("dist")
}

tasks.named("build").configure {
    dependsOn("copyRuntimePacks")
}

/*
tasks.named<KotlinJsCompile>("compileKotlinJs").configure {
    //kotlinOptions.moduleKind = "commonjs"
    //kotlinOptions.noStdlib = false
}

 */
