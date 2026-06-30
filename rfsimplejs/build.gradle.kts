import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
}

description = "Integration with Handicapformidlingen"

configureCommon()
kotest()
kotlin {
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(libs.bundles.kotest)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("com.github.mpe85:grampa:1.6.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
