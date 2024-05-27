import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
}

description = "Models how a helper plan is represented"

configureCommon()
kotest()
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":rfweeks"))
            }
        }
        val commonTest by getting

        val jvmMain by getting
        val jvmTest by getting
    }
}
