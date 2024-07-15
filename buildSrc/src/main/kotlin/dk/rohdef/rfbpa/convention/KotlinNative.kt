package dk.rohdef.rfbpa.convention

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

fun Project.configureCommon() {
    nativeTarget()

    val kotlinLoggingVersion = "6.0.9"
    val arrowKtVersion = "1.2.4"

    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    // Base functionality
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
                    implementation("io.github.oshai:kotlin-logging:$kotlinLoggingVersion")

                    // Base types
                    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
                    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
                    implementation("app.softwork:kotlinx-uuid-core:0.0.25")
                }
            }

            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }

            val jvmMain by getting {
                dependencies {
                    val log4jVersion = "3.0.0-beta2"
                    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
                    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
                    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
                    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

                    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")
                }
            }
        }
    }
}

fun Project.kotest() {
    apply(plugin = "io.kotest.multiplatform")

    val kotestVersion = "5.9.0"
    val arrowKtVersionKotest = "1.4.0"

    kotlin {
        jvm {
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }

        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
                    implementation("io.kotest:kotest-framework-datatest:$kotestVersion")
                    implementation("io.kotest:kotest-framework-engine:$kotestVersion")
                    implementation("io.kotest.extensions:kotest-assertions-arrow:$arrowKtVersionKotest")
                }
            }

            val jvmTest by getting {
                dependencies {
                    implementation("io.kotest:kotest-runner-junit5:$kotestVersion")
                }
            }
        }
    }
}

fun Project.nativeTarget() {
    apply(plugin = "kotlin-multiplatform")

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    kotlin {
        kotlin.applyDefaultHierarchyTemplate()

        jvm {
            withJava()
        }
//        linuxX64()
//        macosX64()
//        macosArm64()
    }
}
