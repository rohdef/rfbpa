package dk.rohdef.rfsimplejs

import com.github.mpe85.grampa.createGrammar
import dk.rohdef.rfsimplejs.ast.Expression

class JavaScriptParser {
    private val grammar = SimpleJavaScript::class.createGrammar()
    private val parser = com.github.mpe85.grampa.parser.Parser(grammar)

    fun parse(javascript: String): Expression {
        val result = parser.run(javascript)

        // TODO make either and handle errors
        return result.stackTop
            ?: throw IllegalArgumentException("Could not parse:\n$javascript")
    }
}