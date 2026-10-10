package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import dk.rohdef.rfsimplejs.ParserSpec
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.grammar.IdentifierGrammar
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.parserContexts
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

class IdentifierTest : FunSpec({
    parserContexts(
        ParserSpec(JavaScriptGrammar::class.createGrammar(), { Program(it) }, "Full JavaScript"),
        ParserSpec(IdentifierGrammar::class.createGrammar(), { it }, "Literal parser"),
    ) { parser, wrapping ->
        withTests(
            "first",
            "\$abe",
            "_yay",
            "æblegrød",
            "down19",
            "a_4_\$_what",
        ) {
            val result = parser.run(it)

            result.stackTop shouldBe wrapping(
                Identifier(it)
            )
        }
    }
})