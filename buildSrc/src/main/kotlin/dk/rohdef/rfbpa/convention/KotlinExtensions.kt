package dk.rohdef.rfbpa.convention

import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*

fun Project.kotlin(block: KotlinMultiplatformExtension.() -> Unit) {
    configure(block)
}

val Project.kotlin: KotlinMultiplatformExtension get() = the()