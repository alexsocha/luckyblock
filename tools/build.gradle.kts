plugins {
    id("org.jetbrains.kotlin.jvm")
    application
    distribution
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.2")
    implementation("br.com.gamemods:nbt-manipulator:3.1.0") 
    implementation("com.charleskorn.kaml:kaml:0.37.0")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")
    implementation("io.github.g00fy2:versioncompare:1.5.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.+")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}


java.sourceSets["main"].java {
    srcDir("../bedrock/src/main/kotlin/common")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("mod.lucky.tools.MainKt")
}

tasks.register<JavaExec>("uploadToCurseForge") {
    classpath = fileTree("$rootDir/tools/build/install/tools/lib")
    mainClass.set("mod.lucky.tools.MainKt")
    args = listOf(
        "upload-to-curseforge",
        "--inputDistFolder",
        "../dist"
    )
    dependsOn("installDist")
}
