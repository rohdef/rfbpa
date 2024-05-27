import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
}

description = "Library for dealing with weeks and kotlinx.datetime"

configureCommon()
kotlin {
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                kotest()
            }
        }
    }
}
