import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.0"
    application
}

description = "Integration with Handicapformidlingen"

configureCommon()
kotest()
application {
    mainClass.set("dk.rohdef.rfbpa.MainKt")
}
kotlin {
    sourceSets {
        val ktorVersion = "2.3.0"

        val commonMain by getting {
            dependencies {
                implementation(project(":helperplanning"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("app.softwork:kotlinx-uuid-core:0.0.18")

                implementation("io.insert-koin:koin-core:3.4.3")

                // TODO terrible library design in clikt, but only usable option here and now
                implementation("com.github.ajalt.clikt:clikt:4.2.0")

                implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
            }
        }
        val commonTest by getting

        val jvmMain by getting {
            dependencies {
                implementation(project(":axpclient"))
            }
        }
        val jvmTest by getting
    }
}