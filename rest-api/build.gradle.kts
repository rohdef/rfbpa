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
    mainClass = "dk.rohdef.rfbpa.web.MainKt"

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
    implementation(libs.kotlinxSerializationYaml)
    implementation(libs.bundles.koin)

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

    implementation("org.mnode.ical4j:ical4j:4.2.5")

    implementation(libs.bundles.persistence)

    // Test
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.kotestJunit)

    testImplementation(libs.kotestKoin)
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
