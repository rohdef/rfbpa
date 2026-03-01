import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

description = "Integration with Handicapformidlingen"

configureCommon()
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":helperplanning"))
                implementation(project(":rfweeks"))

                implementation(libs.bundles.kotlinxSerializationJson)

                implementation(libs.ktorClientCore)
            }
        }
        val commonTest by getting {
            dependencies {
                kotest()
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":rfsimplejs"))
                implementation(libs.jsoup)

                implementation(libs.ktorClientCio)
                implementation(libs.ktorClientLogging)
            }
        }
        val jvmTest by getting
    }
}
