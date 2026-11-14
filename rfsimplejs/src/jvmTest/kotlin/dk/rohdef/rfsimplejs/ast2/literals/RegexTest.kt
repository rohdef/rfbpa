package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.Declaration
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.Assignment
import dk.rohdef.rfsimplejs.ast2.statement.ChainedCall
import dk.rohdef.rfsimplejs.ast2.statement.FunctionCall
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.Return
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.RegexLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Regex literals")
class RegexTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("basic patterns") {
        test("simple alphabetic pattern") {
            val code = "/abc/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("abc"),
            )
        }

        test("alphanumeric pattern") {
            val code = "/pattern123/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("pattern123"),
            )
        }

        test("pattern with spaces") {
            val code = "/hello world/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("hello world"),
            )
        }

        test("single character pattern") {
            val code = "/a/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("a"),
            )
        }

        test("digits only pattern") {
            val code = "/12345/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("12345"),
            )
        }
    }

    context("patterns with flags") {
        test("case-insensitive flag") {
            val code = "/test/i"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("test/i"),
            )
        }

        test("global flag") {
            val code = "/abc/g"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("abc/g"),
            )
        }

        test("multiline flag") {
            val code = "/line/m"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("line/m"),
            )
        }

        test("dotAll flag") {
            val code = "/all/s"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("all/s"),
            )
        }

        test("unicode flag") {
            val code = "/unicode/u"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("unicode/u"),
            )
        }

        test("sticky flag") {
            val code = "/sticky/y"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("sticky/y"),
            )
        }

        test("multiple combined flags") {
            val code = "/search/gi"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("search/gi"),
            )
        }

        test("all standard flags combined") {
            val code = "/complex/gimsuy"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("complex/gimsuy"),
            )
        }
    }

    context("character classes and sets") {
        test("simple character set") {
            val code = "/[abc]/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("[abc]"),
            )
        }

        test("character range") {
            val code = "/[a-z]/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("[a-z]"),
            )
        }

        test("numeric range") {
            val code = "/[0-9]/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("[0-9]"),
            )
        }

        test("multiple alphanumeric ranges with underscore") {
            val code = "/[a-zA-Z0-9_]/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("[a-zA-Z0-9_]"),
            )
        }

        test("negated character class") {
            val code = "/[^0-9]/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("[^0-9]"),
            )
        }

        test("character class with flags") {
            val code = "/[a-z]/i"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("[a-z]/i"),
            )
        }
    }

    context("escape sequences and special characters") {
        test("escaped digits shorthand") {
            val code = "/\\d+/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("\\d+"),
            )
        }

        test("escaped words, whitespace, and digits shorthands") {
            val code = "/\\w+\\s*\\d*/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("\\w+\\s*\\d*"),
            )
        }

        test("escaped slash inside pattern") {
            val code = "/a\\/b/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("a\\/b"),
            )
        }

        test("multiple escaped slashes in URL path pattern") {
            val code = "/\\/api\\/v1\\/users/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("\\/api\\/v1\\/users"),
            )
        }

        test("escaped brackets") {
            val code = "/\\[test\\]/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("\\[test\\]"),
            )
        }

        test("escaped backslash") {
            val code = "/\\\\d+/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("\\\\d+"),
            )
        }
    }

    context("quantifiers, anchors, and groups") {
        test("various quantifiers") {
            val code = "/a*b+c?d{2,4}/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("a*b+c?d{2,4}"),
            )
        }

        test("start and end anchors") {
            val code = "/^start.*end$/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("^start.*end$"),
            )
        }

        test("word boundary anchors") {
            val code = "/\\bword\\b/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("\\bword\\b"),
            )
        }

        test("capturing group with repetition") {
            val code = "/(abc)+/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("(abc)+"),
            )
        }

        test("alternation") {
            val code = "/cat|dog|bird/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("cat|dog|bird"),
            )
        }

        test("non-capturing group") {
            val code = "/(?:foo|bar)/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("(?:foo|bar)"),
            )
        }

        test("named capturing group") {
            val code = "/(?<id>\\d+)/"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                RegexLiteral("(?<id>\\d+)"),
            )
        }
    }

    context("in statements and expressions") {
        test("const declaration") {
            val code = "const pattern = /abc/i;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Constant(
                    mapOf(
                        Identifier("pattern") to RegexLiteral("abc/i"),
                    ),
                ),
            )
        }

        test("let declaration") {
            val code = "let re = /[0-9]+/;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Let(
                    mapOf(
                        Identifier("re") to RegexLiteral("[0-9]+"),
                    ),
                ),
            )
        }

        test("var declaration") {
            val code = "var regex = /\\w+/g;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Variable(
                    mapOf(
                        Identifier("regex") to RegexLiteral("\\w+/g"),
                    ),
                ),
            )
        }

        test("return statement") {
            val code = "return /^[A-Z]/;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Return.Expression(
                    RegexLiteral("^[A-Z]"),
                ),
            )
        }

        test("assignment") {
            val code = "pattern = /new_regex/;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Assignment(
                    Identifier("pattern"),
                    RegexLiteral("new_regex"),
                ),
            )
        }

        test("function call argument") {
            val code = "testRegex(/abc/)"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                FunctionCall(
                    Identifier("testRegex"),
                    listOf(
                        RegexLiteral("abc"),
                    ),
                ),
            )
        }

        test("chained function call argument") {
            val code = "str.replace(/foo/g, \"bar\")"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ChainedCall(
                    Identifier("str"),
                    FunctionCall(
                        Identifier("replace"),
                        listOf(
                            RegexLiteral("foo/g"),
                            StringLiteral("bar"),
                        ),
                    ),
                ),
            )
        }

        test("in array literal") {
            val code = "[/first/, /second/i, /third/g]"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ArrayLiteral(
                    RegexLiteral("first"),
                    RegexLiteral("second/i"),
                    RegexLiteral("third/g"),
                ),
            )
        }

        test("in object literal") {
            val code = "{ pattern: /^[a-z]+$/, flag: /test/ }"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ObjectLiteral(
                    Identifier("pattern") to RegexLiteral("^[a-z]+$"),
                    Identifier("flag") to RegexLiteral("test"),
                ),
            )
        }
    }
})