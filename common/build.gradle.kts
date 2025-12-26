import mod.lucky.build.*

val rootProjectProps = RootProjectProperties.fromProjectYaml(rootProject.rootDir)

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    // todo: separate common and JVM tests when mockk adds multiplatform support
    // implementation("io.mockk:mockk-common:1.+")
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.+")
}

tasks.register<Copy>("testCopyRuntimeResources") {
    into("./build/test-run")
    into("config/lucky/0.0.0-0-test") {
        from("./src/main/resources/lucky-config")
    }
    into("addons/lucky/${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}") {
        from("./src/main/resources/${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}")
    }
}
tasks.getByName("test").dependsOn(tasks.getByName("testCopyRuntimeResources"))

tasks.register<Zip>("buildCustomLuckyBlockJava") {
    val version = rootProjectProps.projects[ProjectName.CUSTOM_LUCKY_BLOCK_JAVA]!!.version
    val distName = "${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}-$version"
    val distDir = file("$rootDir/dist/$distName")
    archiveFileName.set("$distName.zip")
    destinationDirectory.set(file("$rootDir/dist/$distName"))

    doFirst {
        val distMeta = rootProjectProps.getDistMeta(rootDir, ProjectName.CUSTOM_LUCKY_BLOCK_JAVA)
        file(distDir).mkdirs()
        file("$distDir/meta.yaml").writeText(distMeta.toYaml())
    }
    from("src/main/resources/${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}")
    from("$rootDir/dist/$distName/meta.yaml")
}

tasks.getByName<ProcessResources>("processResources") {
    exclude("*")
    dependsOn(tasks.getByName("buildCustomLuckyBlockJava"))
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.LENIENT)
}
