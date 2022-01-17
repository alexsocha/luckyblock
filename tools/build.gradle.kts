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
    testImplementation(kotlin("test"))
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
