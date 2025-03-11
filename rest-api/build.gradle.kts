plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.adarshr.test-logger")
    alias(libs.plugins.ktor)

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
testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

val kotestVersion = "6.0.0.M2"
val arrowKtVersionKotest = "1.4.0"
dependencies {
    implementation(project(":axpclient"))
    implementation(project(":helperplanning"))
    implementation(project(":rfweeks"))

    // Base functionality
    implementation(libs.kotlinxCoroutines)

    // Base types
    implementation(libs.arrowKtCore)
    implementation(libs.kotlinxDateTime)
    implementation("app.softwork:kotlinx-uuid-core:0.0.26")


    implementation(libs.bundles.loggingJvm)
    implementation(libs.bundles.kotlinxSerializationJson)
    implementation(libs.bundles.koin)

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
    implementation("io.ktor:ktor-server-status-pages")

    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    implementation("org.mnode.ical4j:ical4j:4.0.0-rc6")

    val exposed_version = "0.60.0"
    val h2_version = "2.3.232"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.flywaydb:flyway-core:10.15.0")

    // Test
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")

    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-engine:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$arrowKtVersionKotest")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation(libs.koinTest)
}

kotlin {
    jvmToolchain(21)
}

configurations.all {
    resolutionStrategy {
        force("io.netty:netty-transport-native-epoll:4.1.119.Final")
        force("io.netty:netty-transport-native-kqueue:4.1.119.Final")
        force("io.netty:netty-codec-http2:4.1.119.Final")
    }
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
