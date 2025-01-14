plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin") version "3.0.1"
    application
    idea
}

description = "Web server for RF BPA"

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
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

val kotlinLoggingVersion = "7.0.3"
val arrowKtVersion = "1.2.4"
val log4jVersion = "3.0.0-beta2"
val kotestVersion = "5.9.0"
val arrowKtVersionKotest = "1.4.0"
dependencies {
    // Base functionality
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")

    // Base types
    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("app.softwork:kotlinx-uuid-core:0.0.25")

    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")

    implementation(project(":axpclient"))
    implementation(project(":helperplanning"))
    implementation(project(":rfweeks"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")


    val koinVersion = "3.6.0-Beta5"
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    implementation("io.ktor:ktor-client-apache")

    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-auto-head-response")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-resources")

    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation("org.mnode.ical4j:ical4j:4.0.0-rc6")

    val exposed_version = "0.52.0"
    val h2_version = "2.2.224"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.flywaydb:flyway-core:10.15.0")

    // Test
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.0")

    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-engine:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$arrowKtVersionKotest")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
}

configurations.all {
    resolutionStrategy {
        force("io.netty:netty-transport-native-epoll:4.1.115.Final")
        force("io.netty:netty-transport-native-kqueue:4.1.115.Final")
        force("io.netty:netty-codec-http2:4.1.115.Final")
    }
}


idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
