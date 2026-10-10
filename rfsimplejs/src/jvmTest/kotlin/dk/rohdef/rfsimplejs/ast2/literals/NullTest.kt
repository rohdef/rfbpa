package dk.rohdef.rfsimplejs.ast2.literals

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@DisplayName("Null literals")
class NullTest : FunSpec({
    test("literal parser") {
        val grammar = LiteralGrammar::class.createGrammar()
        val parser = Parser(grammar)

        val text = "null"
        val result = parser.run(text).stackTop
        result shouldBe Null
    }

    test("Javascript parser null") {
        val grammar = JavaScriptGrammar::class.createGrammar()
        val parser = Parser(grammar)

        val text = "null"
        val result = parser.run(text).stackTop
        result shouldBe Program(Null)
    }
})