import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.23"
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
        val commonMain by getting {
            dependencies {
                implementation(project(":helperplanning"))
                implementation(project(":rfweeks"))

                implementation("app.softwork:kotlinx-uuid-core:0.0.18")

                implementation("io.insert-koin:koin-core:3.6.0-Beta2")

                // TODO terrible library design in clikt, but only usable option here and now
                implementation("com.github.ajalt.clikt:clikt:4.3.0")

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
