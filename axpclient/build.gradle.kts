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
        val ktorVersion = "2.3.12"

        val commonMain by getting {
            dependencies {
                implementation(project(":helperplanning"))
                implementation(project(":rfweeks"))

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

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
