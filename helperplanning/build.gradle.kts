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
        commonMain.dependencies {
            implementation(project(":rfweeks"))

            implementation(libs.bundles.kotlinxSerializationJson)
        }
        commonTest.dependencies {
            implementation(libs.kotlinxSerializationYaml)
            implementation(libs.bundles.kotest)
        }

        jvmMain.dependencies {}
        jvmTest.dependencies {}
    }
}