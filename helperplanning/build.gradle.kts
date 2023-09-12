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
        val ktorVersion = "2.3.0"

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("app.softwork:kotlinx-uuid-core:0.0.18")
            }
        }
        val commonTest by getting

        val jvmMain by getting
        val jvmTest by getting
    }
}
