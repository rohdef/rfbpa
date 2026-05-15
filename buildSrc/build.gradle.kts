plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradleKotlin)
    implementation(libs.gradleKotlinxSerialization)
    implementation(libs.gradleKotest)
    implementation(libs.gradleTestLogging)
}

dependencyLocking {
    lockMode.set(LockMode.STRICT)
}

configurations {
    compileClasspath { resolutionStrategy.activateDependencyLocking() }
    runtimeClasspath { resolutionStrategy.activateDependencyLocking() }
}
