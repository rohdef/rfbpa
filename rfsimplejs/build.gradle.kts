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
        commonMain.dependencies {}

        commonTest.dependencies {
            implementation(libs.bundles.kotest)
        }

        jvmMain.dependencies {
            implementation("com.github.mpe85:grampa:1.7.0")
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
