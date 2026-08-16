package dk.rohdef.rfsimplejs

import com.github.mpe85.grampa.grammar.Grammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class ParserSpec(
    val grammar: Grammar<Statement>,
    val wrapping: (Statement)  -> Statement,
    val name: String,
) {
    val parser = Parser(grammar)
}