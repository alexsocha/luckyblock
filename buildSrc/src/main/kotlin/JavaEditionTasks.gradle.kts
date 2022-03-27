package mod.lucky.build

val rootProjectProps = RootProjectProperties.fromProjectYaml(rootProject.rootDir)

val projectName = when(project.name) {
    "forge" -> ProjectName.LUCKY_BLOCK_FORGE
    "fabric" -> ProjectName.LUCKY_BLOCK_FABRIC
    else -> throw Exception("Project name should be 'forge' or 'fabric'")
}
val projectProps = rootProjectProps.projects[projectName]!!

tasks.register<Copy>("copyRuntimeResources") {
    into("$rootDir/run")
    into("config/lucky/${projectProps.version}-${projectName.shortName}") {
        from("$rootDir/common/src/jvmMain/resources/lucky-config")
    }
    into("addons/lucky/lucky-block-custom") {
        from("$rootDir/common/src/jvmMain/resources/${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}")
    }
}

tasks.register<Zip>("luckyBlockConfigDist") {
    archiveFileName.set("lucky-config.zip")
    destinationDirectory.set(file("$rootDir/common/build/tmp"))
    from("src/jvmMain/resources/lucky-config")
}

tasks.register<Zip>("exportDist") {
    val distName = "${rootProject.name}-${projectName.shortName}-${projectProps.version}"
    val distDir = file("$rootDir/dist/$distName")
    destinationDirectory.set(distDir)
    archiveFileName.set("$distName.jar")

    doFirst {
        val distMeta = rootProjectProps.getDistMeta(rootDir, projectName)
        file(distDir).mkdirs()
        file("$distDir/meta.yaml").writeText(distMeta.toYaml())
    }
    from(zipTree("./build/libs/${rootProject.name}-${projectProps.version}.jar"))
    from("$rootDir/common/build/tmp/lucky-config.zip") { into("mod/lucky/java") }
    from("$rootDir/dist/$distName/meta.yaml")

    dependsOn(tasks.getByName("luckyBlockConfigDist"))
    dependsOn(tasks.getByName("jar"))
}

tasks.register<Delete>("cleanDist") {
    delete("$rootDir/dist")
}
