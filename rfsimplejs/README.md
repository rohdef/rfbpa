# RF Simple JS

A JavaScript for creating a simple Abstract Syntax Tree (AST).
This is designed and intended to make it possible to extract and analyze data in JavaScript.

It is not guaranteed to be fully correct and will almost certainly allow constructs that are not valid.
JavaScript contructs for modules (`import`, `export`, etc.) is currently not supported.


# Deliberately postponed/not supported

- statement
  - template-literal
  - regex
  - postfix increment/decrement
  - class
  - all module logic, export, import etc.b
  - bitwise logic
  - some of the less used assignments (eg., `<<==`)

Probably a good idea to support empty statements as a unit, rather than using block (it is kind of a hack)
