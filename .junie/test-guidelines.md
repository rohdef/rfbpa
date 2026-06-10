### Test Guidelines

These guidelines supplement the main project guidelines with specific rules for writing tests in this project.

#### 1. General Principles

- **No Mocks**: Avoid using mocking frameworks (like Mockk or Mockito). Prefer using real implementations, stubs, or fakes if necessary. The current codebase relies on domain objects and functional patterns that reduce the need for mocks.
- **Separation of Concerns**: Clearly separate the action being tested from the assertion. Use intermediate `val` declarations to store results before asserting on them.
- **Functional Assertions**: When testing code that returns `Either`, use Arrow's Kotest extensions (`shouldBeRight()`, `shouldBeLeft()`) for assertions.

#### 2. Code Style in Tests

- **Named Parameters**: Follow the main project rule: do not use named parameters unless they are necessary for disambiguation.
- **Kotest Style**: Use the `FunSpec` style.
- **Test Names**: Use descriptive string names for tests.

#### 3. Examples

Based on the `ShiftTest.kt` implementation:

```kotlin
class ShiftTest : FunSpec({
    val start = YearWeekDayAtTime.parseUnsafe("2024-W24-1T10:00")
    val end = YearWeekDayAtTime.parseUnsafe("2024-W24-1T12:00")

    test("start must be before end") {
        val afterEnd = YearWeekDayAtTime.parseUnsafe("2024-W24-1T13:00")

        // Action
        val success = Shift.create(
            HelperBooking.NoBooking,
            start,
            end,
        )
        // Assertion
        success.shouldBeRight()

        // Action
        val failure = Shift.create(
            HelperBooking.NoBooking,
            afterEnd,
            end,
        )
        // Assertion
        failure.shouldBeLeft(Shift.ShiftError.StartAfterEnd(afterEnd, end))
    }
})
```

Note the clear separation between the creation of the result and the assertion on it.
