import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.23"
}

description = "Integration with Handicapformidlingen"

configureCommon()
kotlin {
    sourceSets {
        val ktorVersion = "3.0.0-beta-1"

        val commonMain by getting {
            dependencies {
                implementation(project(":helperplanning"))
                implementation(project(":rfweeks"))

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
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
                implementation("org.jsoup:jsoup:1.17.2")

                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
            }
        }
        val jvmTest by getting
    }
}
