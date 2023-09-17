import dk.rohdef.rfbpa.convention.configureCommon
import dk.rohdef.rfbpa.convention.kotest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.9.10"
}

description = "Integration with Handicapformidlingen"

configureCommon()
kotlin {
    sourceSets {
        val ktorVersion = "2.3.2"

        val commonMain by getting {
            dependencies {
                implementation(project(":helperplanning"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

                implementation("app.softwork:kotlinx-uuid-core:0.0.18")
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
                implementation("org.jsoup:jsoup:1.16.1")

                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
            }
        }
        val jvmTest by getting
    }
}