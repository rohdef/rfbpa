plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.23"
    application
    idea
}

description = "Web server for RF BPA"

//configureCommon()
//kotest()
application {
    applicationName = "rfbpa-web"
    mainClass.set("dk.rohdef.rfbpa.web.MainKt")

    tasks.run.get().workingDir = rootProject.projectDir
}
tasks.getByName<Zip>("distZip") {
    archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
}
tasks.getByName<Tar>("distTar") {
    archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

val kotlinLoggingVersion = "6.0.9"
val arrowKtVersion = "1.2.4"
val log4jVersion = "3.0.0-beta2"
val kotestVersion = "5.9.0"
val arrowKtVersionKotest = "1.4.0"
dependencies {
    // Base functionality
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta")
    implementation("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")

    // Base types
    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
    implementation("app.softwork:kotlinx-uuid-core:0.0.25")

    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")


    implementation(project(":app"))
    implementation(project(":helperplanning"))
    implementation(project(":rfweeks"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("app.softwork:kotlinx-uuid-core:0.0.18")

    implementation("io.insert-koin:koin-core:3.6.0-Beta2")

    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")

    val ktor_version = "3.0.0-beta-1"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("org.mnode.ical4j:ical4j:4.0.0-rc6")

    // Test=
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-engine:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$arrowKtVersionKotest")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}



idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
