import mod.lucky.build.*

val rootProjectProps = RootProjectProperties.fromProjectYaml(rootProject.rootDir)

plugins {
    // waiting on https://github.com/gradle/gradle/issues/9830 to use project.yaml for versions
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {}

    sourceSets {
        val commonMain by getting

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
}

tasks.register<Copy>("jvmTestCopyRuntimeResources") {
    into("./build/test-run")
    into("config/lucky/0.0.0-0-test") {
        from("./src/jvmMain/resources/lucky-config")
    }
    into("addons/lucky/${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}") {
        from("./src/jvmMain/resources/${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}")
    }
}
tasks.getByName("jvmTest").dependsOn(tasks.getByName("jvmTestCopyRuntimeResources"))

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
    from("src/jvmMain/resources/${ProjectName.CUSTOM_LUCKY_BLOCK_JAVA.fullName}")
    from("$rootDir/dist/$distName/meta.yaml")
}

tasks.getByName<ProcessResources>("jvmProcessResources") {
    exclude("*")
    dependsOn(tasks.getByName("buildCustomLuckyBlockJava"))
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.LENIENT)
}
