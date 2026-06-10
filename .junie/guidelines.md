### Project Guidelines

This project is a Kotlin Multiplatform (KMP) system designed for helper planning and salary system integration.

#### 1. Build and Configuration

The project uses Gradle with the Kotlin DSL. It is organized into several subprojects:
- `rest-api`: The JVM-based Ktor web server.
- `helperplanning`: Core business logic (Multiplatform).
- `axpclient`: Client for interacting with the AXP salary system (JVM).
- `rfweeks`: Utility library for handling week-based dates (Multiplatform).
- `rfsimplejs`: Frontend components (Kotlin/JS).

**Prerequisites:**
- JDK 21 (configured via `jvmToolchain` in several modules).
- Gradle (use the provided `./gradlew` wrapper).

**Build Commands:**
- Build the entire project: `./gradlew build`
- Run the web server: `./gradlew :rest-api:run`
- Clean the build: `./gradlew clean`

**Dependency Management:**
- Dependencies are managed in `gradle/libs.versions.toml` using Gradle version catalogs.
- Strict dependency locking is enabled in the root `build.gradle.kts`.

#### 2. Testing

The project uses **Kotest** as the primary testing framework. For detailed testing rules, see [Test Guidelines](test-guidelines.md).

**Running Tests:**
- Run all tests: `./gradlew test` (or `allTests` for Multiplatform modules).
- Run tests for a specific module: `./gradlew :<module-name>:test`.
- Run a specific test class (JVM): `./gradlew :<module-name>:jvmTest --tests "dk.rohdef.package.ClassName"`.

**Adding New Tests:**
- Tests should be placed in `src/commonTest` (for KMP logic) or `src/jvmTest` (for JVM-specific logic).
- Use the `FunSpec` style from Kotest, as it's the prevalent style in this codebase.
- Assertions should use Kotest's `shouldBe` and Arrow's Kotest extensions (e.g., `shouldBeRight()`, `shouldBeLeft()`) where applicable.

**Test Example:**
```kotlin
package dk.rohdef.rfweeks

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GuidelinesDemoTest : FunSpec({
    test("simple addition test") {
        (1 + 1) shouldBe 2
    }
})
```

#### 3. Development and Code Style

- **Functional Programming**: The project extensively uses [Arrow](https://arrow-kt.io/) for functional programming patterns, especially `Either` for error handling.
- **Serialization**: `kotlinx.serialization` is used for JSON and YAML.
- **Date/Time**: `kotlinx-datetime` and the custom `rfweeks` library are used for time-related logic.
- **Code Style**:
    - Follow standard Kotlin coding conventions.
    - Prefer `Either` over exceptions for domain-level errors.
    - Use trailing commas in lists and parameters.
    - Maintain consistent indentation as seen in existing files (4 spaces).
    - **Named Parameters**: Do not use named parameters unless they are necessary to disambiguate cases, such as when dealing with multiple parameters of the same type or using default parameters.
- **Concurrency**: Use Coroutines for asynchronous operations, especially in `rest-api`.
