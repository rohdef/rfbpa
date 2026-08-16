package dk.rohdef.rfbpa.convention

import com.adarshr.gradle.testlogger.TestLoggerExtension
import libs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureCommon() {
    nativeTarget()

    configure<KotlinMultiplatformExtension> {
        compilerOptions {
            freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
        }

        sourceSets {
            commonMain.dependencies {
                // Base functionality
                implementation(libs.kotlinxCoroutines)
                implementation(libs.kotlinLogging)
                implementation("com.marcinmoskala:DiscreteMathToolkit:1.0.3")

                // Base types
                implementation(libs.arrowKtCore)
                implementation(libs.kotlinxDateTime)
            }

            commonTest.dependencies {
            }

            jvmMain.dependencies {
                implementation(libs.bundles.loggingJvm)
            }
        }
    }
}

fun Project.kotest() {
    apply(plugin = "io.kotest")
    apply(plugin = "com.adarshr.test-logger")

    configure<KotlinMultiplatformExtension> {
        jvm {
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }

        sourceSets {
            commonTest.dependencies {
                implementation(libs.bundles.kotest)
            }

            jvmTest.dependencies {
                implementation(libs.bundles.kotestJunit)
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

        jvmToolchain(25)
        jvm()
//        linuxX64()
//        macosX64()
//        macosArm64()
    }
}
