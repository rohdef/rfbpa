import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
}

description = "Integration with Handicapformidlingen"

configureCommon()
kotlin {
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                kotest()
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("com.github.mpe85:grampa:1.2.0")
            }
        }
        val jvmTest by getting
    }
}
