package dk.rohdef.rfbpa.convention

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun Project.configureCommon() {
    nativeTarget()

    val kotlinLoggingVersion = "3.0.4"
    val arrowKtVersion = "1.1.3"

    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")

                    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
                }
            }

            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }

            val jvmMain by getting {
                dependencies {
                    val log4jVersion = "2.20.0"
                    implementation("org.slf4j:slf4j-api:2.0.7")
                    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
                    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
                    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

                    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
                }
            }
        }
    }
}

fun Project.kotest() {
    apply(plugin = "io.kotest.multiplatform")

    val kotestVersion = "5.6.2"
    val arrowKtVersionKotest = "1.3.3"

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
        targetHierarchy.default()

        jvm {
            withJava()
        }
//        linuxX64()
//        macosX64()
//        macosArm64()
    }
}