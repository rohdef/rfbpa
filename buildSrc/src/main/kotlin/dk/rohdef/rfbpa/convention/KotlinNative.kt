package dk.rohdef.rfbpa.convention

import com.adarshr.gradle.testlogger.TestLoggerExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import libs

fun Project.configureCommon() {
    nativeTarget()

    configure<KotlinMultiplatformExtension> {
        compilerOptions {
            freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
        }

        sourceSets {
            val commonMain by getting {
                dependencies {
                    // Base functionality
                    implementation(libs.kotlinxCoroutines)
                    implementation(libs.kotlinLogging)
                    implementation("com.marcinmoskala:DiscreteMathToolkit:1.0.3")

                    // Base types
                    implementation(libs.arrowKtCore)
                    implementation(libs.kotlinxDateTime)
                    implementation("app.softwork:kotlinx-uuid-core:0.0.26")
                }
            }

            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }

            val jvmMain by getting {
                dependencies {
                    implementation(libs.bundles.loggingJvm)
                }
            }
        }
    }
}

fun Project.kotest() {
    apply(plugin = "io.kotest.multiplatform")
    apply(plugin = "com.adarshr.test-logger")

    val kotestVersion = "6.0.0.M2"
    val arrowKtVersionKotest = "2.0.0"

    configure<KotlinMultiplatformExtension> {
        jvm {
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }

        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation("io.kotest:kotest-assertions-core:$kotestVersion")
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

    configure<TestLoggerExtension> {
        theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
    }
}

fun Project.nativeTarget() {
    apply(plugin = "kotlin-multiplatform")

    configure<KotlinMultiplatformExtension> {
        applyDefaultHierarchyTemplate()

        jvmToolchain(21)
        jvm {
            withJava()
        }
//        linuxX64()
//        macosX64()
//        macosArm64()
    }
}
