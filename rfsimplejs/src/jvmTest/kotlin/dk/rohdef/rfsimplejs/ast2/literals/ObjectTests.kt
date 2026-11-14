package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Undefined
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Object literals")
class ObjectTests : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("empty") {
        val code = "{}"

        val result = parser.run(code)

        result.stackTop shouldBe Program(
            ObjectLiteral(),
        )
    }

    test("same line") {
        val code = """
            { day: "Tag", bar: 19 }
        """.trimIndent()

        val result = parser.run(code)

        result.stackTop shouldBe Program(
            ObjectLiteral(
                Identifier("day") to StringLiteral("Tag"),
                Identifier("bar") to Number.SimpleNumber.IntegerNumber(19),
            ),
        )
    }

    test("multiple lines with trailing comma") {
        val code = """
            { 
                day: "Tag",
                bar: 19,
            }
        """.trimIndent()

        val result = parser.run(code)

        result.stackTop shouldBe Program(
            ObjectLiteral(
                Identifier("day") to StringLiteral("Tag"),
                Identifier("bar") to Number.SimpleNumber.IntegerNumber(19),
            ),
        )
    }

    context("literal keys") {
        context("single quote strings") {
            test("single quote string key") {
                val code = "{ 'day': 'Tag' }"

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        StringLiteral("day") to StringLiteral("Tag"),
                    ),
                )
            }

            test("single quote string key with spaces and special characters") {
                val code = """
                    { 'first name': 'John', 'escape\'quote': 42 }
                """.trimIndent()

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        StringLiteral("first name") to StringLiteral("John"),
                        StringLiteral("escape'quote") to Number.SimpleNumber.IntegerNumber(42),
                    ),
                )
            }

            test("empty single quote string key") {
                val code = "{ '': 'empty key' }"

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        StringLiteral("") to StringLiteral("empty key"),
                    ),
                )
            }
        }

        context("double quote strings") {
            test("double quote string key") {
                val code = """{ "title": "Developer" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        StringLiteral("title") to StringLiteral("Developer"),
                    ),
                )
            }

            test("double quote string key with spaces and escaped quotes") {
                val code = """
                    { "full name": "Jane Doe", "say \"hi\"": 100 }
                """.trimIndent()

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        StringLiteral("full name") to StringLiteral("Jane Doe"),
                        StringLiteral("say \"hi\"") to Number.SimpleNumber.IntegerNumber(100),
                    ),
                )
            }

            test("empty double quote string key") {
                val code = """{ "": "empty key" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        StringLiteral("") to StringLiteral("empty key"),
                    ),
                )
            }
        }

        context("numbers") {
            test("integer keys") {
                val code = """{ 1: "one", 42: "answer", 0: "zero" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Number.SimpleNumber.IntegerNumber(1) to StringLiteral("one"),
                        Number.SimpleNumber.IntegerNumber(42) to StringLiteral("answer"),
                        Number.SimpleNumber.IntegerNumber(0) to StringLiteral("zero"),
                    ),
                )
            }

            test("decimal / floating point keys") {
                val code = """{ 3.14: "pi", 0.5: "half", .25: "quarter" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Number.SimpleNumber.DecimalNumber("3.14".toBigDecimal()) to StringLiteral("pi"),
                        Number.SimpleNumber.DecimalNumber("0.5".toBigDecimal()) to StringLiteral("half"),
                        Number.SimpleNumber.DecimalNumber(".25".toBigDecimal()) to StringLiteral("quarter"),
                    ),
                )
            }

            test("hexadecimal number key") {
                val code = """{ 0xff: "hex value" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Number.HexadecimalNumber("0xff") to StringLiteral("hex value"),
                    ),
                )
            }

            test("binary number key") {
                val code = """{ 0b1010: "binary value" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Number.BinaryNumber("0b1010") to StringLiteral("binary value"),
                    ),
                )
            }

            test("scientific number key") {
                val code = """{ 1e3: "thousand" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Number.ScientificNumber(
                            Number.SimpleNumber.IntegerNumber(1),
                            Number.SimpleNumber.IntegerNumber(3),
                        ) to StringLiteral("thousand"),
                    ),
                )
            }
        }

        context("booleans") {
            test("true key") {
                val code = """{ true: "truthy" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Boolean.True to StringLiteral("truthy"),
                    ),
                )
            }

            test("false key") {
                val code = """{ false: "falsy" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Boolean.False to StringLiteral("falsy"),
                    ),
                )
            }

            test("both boolean keys") {
                val code = """{ true: 1, false: 0 }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Boolean.True to Number.SimpleNumber.IntegerNumber(1),
                        Boolean.False to Number.SimpleNumber.IntegerNumber(0),
                    ),
                )
            }
        }

        context("undefined") {
            test("undefined key") {
                val code = """{ undefined: "not defined" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Undefined to StringLiteral("not defined"),
                    ),
                )
            }
        }

        context("null") {
            test("null key") {
                val code = """{ null: "is null" }"""

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Null to StringLiteral("is null"),
                    ),
                )
            }
        }

        context("mixed literal keys") {
            test("object with identifier and various literal keys") {
                val code = """
                    {
                        id: 1,
                        'single': "quote",
                        "double": 'quote',
                        42: true,
                        true: false,
                        null: undefined,
                        undefined: null,
                    }
                """.trimIndent()

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        Identifier("id") to Number.SimpleNumber.IntegerNumber(1),
                        StringLiteral("single") to StringLiteral("quote"),
                        StringLiteral("double") to StringLiteral("quote"),
                        Number.SimpleNumber.IntegerNumber(42) to Boolean.True,
                        Boolean.True to Boolean.False,
                        Null to Undefined,
                        Undefined to Null,
                    ),
                )
            }

            test("nested object literals with literal keys") {
                val code = """
                    {
                        'outer': {
                            1: "inner one",
                            true: {
                                null: "deep",
                            },
                        },
                    }
                """.trimIndent()

                val result = parser.run(code)

                result.stackTop shouldBe Program(
                    ObjectLiteral(
                        StringLiteral("outer") to ObjectLiteral(
                            Number.SimpleNumber.IntegerNumber(1) to StringLiteral("inner one"),
                            Boolean.True to ObjectLiteral(
                                Null to StringLiteral("deep"),
                            ),
                        ),
                    ),
                )
            }
        }
    }

    context("invalid key types") {
        test("array as key is not supported") {
            val code = """{ [1, 2]: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("empty array as key is not supported") {
            val code = """{ []: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("anonymous function as key is not supported") {
            val code = """{ function() {}: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("named function as key is not supported") {
            val code = """{ function foo() { return 1; }: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("one shot lambda with parenthesized parameters as key is not supported") {
            val code = """{ () => {}: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("one shot lambda with single identifier parameter as key is not supported") {
            val code = """{ x => x: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("one shot lambda expression body as key is not supported") {
            val code = """{ (() => 42): "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("regex literal as key is not supported") {
            val code = """{ /abc/g: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("regex literal without flags as key is not supported") {
            val code = """{ /[0-9]+/: 123 }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }

        test("object literal as key is not supported") {
            val code = """{ {}: "value" }"""

            val result = parser.run(code)

            result.matched shouldBe false
        }
    }
})