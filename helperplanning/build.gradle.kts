import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

description = "Models how a helper plan is represented"

configureCommon()
kotest()
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":rfweeks"))

                implementation(libs.bundles.kotlinxSerializationJson)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlinxSerializationYaml)
            }
        }

        val jvmMain by getting
        val jvmTest by getting
    }
}