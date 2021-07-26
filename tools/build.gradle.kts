plugins {
    id("org.jetbrains.kotlin.jvm")
    application
    distribution
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

fun createAdditionalScript(name: String, configureStartScripts: CreateStartScripts.() -> Unit) =
    tasks.register<CreateStartScripts>("startScripts$name") {
        configureStartScripts()
        applicationName = name
        outputDir = File(project.buildDir, "scripts")
        classpath = tasks.getByName("jar").outputs.files + configurations.runtimeClasspath.get()
    }.also {
        application.applicationDistribution.into("bin") {
            from(it)
            fileMode = 0b000_111_101_101
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }

application {
    mainClass.set("mod.lucky.tools.GenerateBedrockConfigKt")
}

/*
createAdditionalScript("sample") {
    mainClass.set("mod.lucky.tools.SampleScript")
}
*/