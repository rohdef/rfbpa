plugins {
    `kotlin-dsl`

    // don't add the Kotlin JVM plugin
    // kotlin("jvm") version embeddedKotlinVersion
    // Why? It's a long story, but Gradle uses an embedded version of Kotlin,
    // (which is provided by the `kotlin-dsl` plugin)
    // which means importing an external version _might_ cause issues
    // It's annoying but not important. The Kotlin plugin version below,
    // in dependencies { }, will be used for building our 'main' project.
    // https://github.com/gradle/gradle/issues/16345
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    val kotlinVersion = "2.1.0"
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    implementation("io.kotest:kotest-framework-multiplatform-plugin-gradle:6.0.0.M1")
}

dependencyLocking {
    lockMode.set(LockMode.STRICT)
}

configurations {
    compileClasspath { resolutionStrategy.activateDependencyLocking() }
    runtimeClasspath { resolutionStrategy.activateDependencyLocking() }
}
