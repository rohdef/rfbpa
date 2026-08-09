import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

description = "Integration with MitHF"

configureCommon()
kotest()
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":helperplanning"))
                implementation(project(":rfweeks"))

                implementation(libs.arrowKtCore)
                implementation(libs.kotlinxDateTime)

                implementation(libs.bundles.kotlinxSerializationJson)


                implementation("com.fleeksoft.ksoup:ksoup:0.2.6")
                implementation(libs.bundles.ktorClient)
                implementation(libs.bundles.kotlinxSerializationJson)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotest)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":rfsimplejs"))

                implementation(libs.jsoup)
                implementation(libs.bundles.loggingJvm)
            }
        }
        val jvmTest by getting
    }
}
