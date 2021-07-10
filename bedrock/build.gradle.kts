//import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
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

kotlin {
    js {
        browser {
            useCommonJs()
            dceTask {
                keep("luckyblock-bedrock.mod.lucky.bedrock.sayHello")
            }
        }
    }
}

task<NodeTask>("webpack") {
    setScript(File("$rootDir/build/js/node_modules/webpack/bin/webpack"))

    dependsOn(tasks.getByName("jsJar"))
}

tasks.named("assemble").configure {
    dependsOn("webpack")
}

/*
tasks.named<KotlinJsCompile>("compileKotlinJs").configure {
    //kotlinOptions.moduleKind = "commonjs"
    //kotlinOptions.noStdlib = false
}

 */