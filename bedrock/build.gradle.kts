import java.io.File
import java.util.UUID
import org.apache.commons.text.CaseUtils
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
        classpath("org.apache.commons:commons-text:1.9")
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
    args = listOf(
        "generate-bedrock-drops",
        "--inputConfigFolder",
        "$rootDir/bedrock/src/main/resources/lucky-config",
        "--inputJsTemplateFile",
        "$rootDir/bedrock/src/main/resources/serverScript.template.js",
        "--blockId",
        "lucky_block",
        "--outputJsFile",
        "$rootDir/bedrock/build/processedResources/serverScript.js",
        "--outputStructuresFolder",
        "$addonDistDir/behavior_pack/structures/lucky",
    )

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
        "$rootDir/tools/block_conversion.yaml",
        "--outputGeneratedBlockConversionFile",
        "$rootDir/tools/.debug/block_conversion.generated.yaml",
    )
    dependsOn(project(":tools").tasks.getByName("installDist"))
}

tasks.register<Copy>("copyTemplateAddon") {
    val bedrockTemplateAddonBlockId = "custom_lucky_block"

    fun getUuid(suffix: String): String {
        val bedrockTemplateAddonUuid = UUID.nameUUIDFromBytes(
            "lucky:${bedrockTemplateAddonBlockId}".toByteArray()
        ).toString()
        return UUID.nameUUIDFromBytes("${bedrockTemplateAddonUuid}-${suffix}".toByteArray()).toString()
    }

    doFirst {
        delete(fileTree("./build/processedResources/template-addon"))
    }

    from("./template-addon")
    into("./build/template-addon")

    inputs.property("blockId", bedrockTemplateAddonBlockId)
    filesMatching(listOf("**/*.json", "**/*.lang")) {
        expand(
            "blockId" to bedrockTemplateAddonBlockId,
            "addonId" to CaseUtils.toCamelCase(bedrockTemplateAddonBlockId, true, '_'),
            "blockName" to bedrockTemplateAddonBlockId.split("_").joinToString(" ") {
                it.capitalize() // TODO: use replaceFirstChar once gradle is updated to Kotlin 1.6
            },
            "behaviorPackUuid" to getUuid("behavior-pack"),
            "behaviorPackModuleUuid" to getUuid("behavior-pack-module"),
            "resourcePackUuid" to getUuid("resource-pack"),
            "resourcePackModuleUuid" to getUuid("resource-pack-module"),
        )
    }
    rename { fileName ->
        fileName.replace("\${blockId}", "$bedrockTemplateAddonBlockId")
    }
}

tasks.register<JavaExec>("buildTemplateAddon") {
    classpath = fileTree("$rootDir/tools/build/install/tools/lib")
    mainClass.set("mod.lucky.tools.MainKt")
    args = listOf(
        "generate-bedrock-config",
        "--inputConfigFolder",
        "./template-addon",
        "--outputAddonFolder",
        "./build/template-addon",
    )
    dependsOn(project(":tools").tasks.getByName("installDist"))
    dependsOn("copyTemplateAddon")
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

tasks.register<Zip>("exportDist") {
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
    tasks.getByName("browserProductionWebpack").dependsOn("generateBedrockDrops")
    tasks.getByName("browserProductionWebpack").inputs.file("./build/processedResources/serverScript.js")
    tasks.getByName("copyServerScript").dependsOn("browserProductionWebpack")
    tasks.getByName("exportDist").dependsOn("browserProductionWebpack")
    dependsOn("browserProductionWebpack")
    dependsOn("copyServerScript")
    dependsOn("exportDist")
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

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.LENIENT)
}
