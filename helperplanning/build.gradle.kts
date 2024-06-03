import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.23"
}

description = "Models how a helper plan is represented"

configureCommon()
kotest()
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":rfweeks"))

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }
        val commonTest by getting

        val jvmMain by getting
        val jvmTest by getting
    }
}
