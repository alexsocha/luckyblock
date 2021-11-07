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
