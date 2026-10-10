package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import dk.rohdef.rfsimplejs.ParserSpec
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.grammar.literal.StringGrammar
import dk.rohdef.rfsimplejs.parserContexts
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

@DisplayName("String literals")
class StringTest : FunSpec({
    parserContexts(
        ParserSpec(JavaScriptGrammar::class.createGrammar(), { Program(it) }, "Full JavaScript"),
        ParserSpec(LiteralGrammar::class.createGrammar(), { it }, "Literal parser"),
        ParserSpec(StringGrammar::class.createGrammar(), { it }, "String parser"),
    ) { parser, wrapping ->
        withTests(
            """
                ""
            """.trimIndent(),
            """
                "Hello, world!"
            """.trimIndent(),
            """
                "\"Hello, world!\""
            """.trimIndent(),
            """
                'What a wonderful thing you are'
            """.trimIndent(),
            """
                "The song 'Peace somehow'"
            """.trimIndent(),
            """
                "A quote looks like: \" and escape should work"
            """.trimIndent(),
            """
                'Single quote : \' like this'
            """.trimIndent(),
        ) {
            val result = parser.run(it)

            val stringWithoutQuotes = it.substring(1, it.length - 1)
            result.stackTop shouldBe wrapping(
                StringLiteral(stringWithoutQuotes)
            )
        }
    }
})