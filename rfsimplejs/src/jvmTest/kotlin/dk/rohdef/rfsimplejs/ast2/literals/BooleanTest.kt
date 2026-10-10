package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ParserSpec
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withContexts
import io.kotest.matchers.shouldBe

class BooleanTest : FunSpec({
    withContexts(
        nameFn = { it.name },
        ParserSpec(JavaScriptGrammar::class.createGrammar(), { Program(it) }, "Full JavaScript"),
        ParserSpec(LiteralGrammar::class.createGrammar(), { it }, "Literal parser"),
    ) { spec ->
        test("true") {
            val text = "true"
            val result = spec.parser.run(text).stackTop
            result shouldBe spec.wrapping(Boolean.True)
        }

        test("false") {
            val text = "false"
            val result = spec.parser.run(text).stackTop
            result shouldBe spec.wrapping(Boolean.False)
        }

    }
})