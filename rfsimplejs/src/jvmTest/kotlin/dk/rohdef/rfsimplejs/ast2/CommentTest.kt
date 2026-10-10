package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Comments")
class CommentTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    context("rest of line comments") {
        test("only a line comment") {
            val text = "// this is a comment"

            val result = parser.run(text).stackTop

            result shouldBe Program()
        }

        test("multiple line comments") {
            val text = """
                // first comment
                // second comment
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program()
        }

        test("line comment with special characters and symbols") {
            val text = """
                // !@#$%^&*()_+=-{}[]:;"'<>,.?/
                // function fake() { return 123; }
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program()
        }

        test("line comment after statement") {
            val text = "foo(); // trailing comment"

            val result = parser.run(text).stackTop

            result shouldBe Program(
                FunctionCall(Identifier("foo")),
            )
        }

        test("line comment between statements") {
            val text = """
                foo();
                // middle comment
                bar();
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program(
                FunctionCall(Identifier("foo")),
                FunctionCall(Identifier("bar")),
            )
        }

        test("line comments mixed with declarations and expressions") {
            val text = """
                // declare variable
                let a = 1; // initialized to 1
                // compute sum
                let b = a + 2;
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program(
                Declaration.Let(
                    Identifier("a") to Number.SimpleNumber.IntegerNumber(1),
                ),
                Declaration.Let(
                    Identifier("b") to ArithmeticExpression.Addition(
                        Identifier("a"),
                        Number.SimpleNumber.IntegerNumber(2),
                    ),
                ),
            )
        }
    }

    context("block comments") {
        test("only a single-line block comment") {
            val text = "/* simple comment */"

            val result = parser.run(text).stackTop

            result shouldBe Program()
        }

        test("multi-line block comment") {
            val text = """
                /*
                 * Multi-line description
                 * with details
                 */
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program()
        }

        test("block comment with asterisks and slashes inside") {
            val text = "/*** JSDoc style * with ** multiple *** stars ***/"

            val result = parser.run(text).stackTop

            result shouldBe Program()
        }

        test("block comment spanning multiple lines before and after statements") {
            val text = """
                /*
                 * Start of program
                 */
                foo();
                /*
                 * End of program
                 */
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program(
                FunctionCall(Identifier("foo")),
            )
        }

        test("inline block comment inside function call arguments") {
            val text = "foo(a, b, /* not sure yet */ d, e);"

            val result = parser.run(text).stackTop

            result shouldBe Program(
                FunctionCall(
                    Identifier("foo"),
                    Identifier("a"),
                    Identifier("b"),
                    Identifier("d"),
                    Identifier("e"),
                ),
            )
        }

        test("inline block comments within statement") {
            val text = "/* leading */ foo(/* arg */ a /* after */);"

            val result = parser.run(text).stackTop

            result shouldBe Program(
                FunctionCall(
                    Identifier("foo"),
                    Identifier("a"),
                ),
            )
        }

        test("block comment spanning multiple lines inside function call arguments") {
            val text = """
                foo(
                    a,
                    /*
                     * multiline comment
                     * explaining b
                     */
                    b,
                    c
                );
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program(
                FunctionCall(
                    Identifier("foo"),
                    Identifier("a"),
                    Identifier("b"),
                    Identifier("c"),
                ),
            )
        }

        test("block comment inside array and object literals") {
            val text = """
                let arr = [1, /* second */ 2, 3];
                let obj = {
                    /* key a */
                    a: "hello",
                    /* key b */
                    b: 42
                };
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program(
                Declaration.Let(
                    Identifier("arr") to ArrayLiteral(
                        Number.SimpleNumber.IntegerNumber(1),
                        Number.SimpleNumber.IntegerNumber(2),
                        Number.SimpleNumber.IntegerNumber(3),
                    ),
                ),
                Declaration.Let(
                    Identifier("obj") to ObjectLiteral(
                        Identifier("a") to StringLiteral("hello"),
                        Identifier("b") to Number.SimpleNumber.IntegerNumber(42),
                    ),
                ),
            )
        }

        test("block comments inside function definition") {
            val text = """
                function add(/* first */ a, /* second */ b) {
                    /* calculate and return */
                    return a + b;
                }
            """.trimIndent()

            val result = parser.run(text).stackTop

            result shouldBe Program(
                Declaration.Variable(
                    Identifier("add") to FunctionExpression(
                        FunctionExpression.Parameters()
                            .withParameter(Identifier("a"))
                            .withParameter(Identifier("b")),
                        BlockStatement(
                            Return.Expression(
                                ArithmeticExpression.Addition(
                                    Identifier("a"),
                                    Identifier("b"),
                                ),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
})
