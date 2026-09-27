package dk.rohdef.rfsimplejs.ast2.statement.logic

import dk.rohdef.rfsimplejs.ast2.statement.BlockStatement
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement

sealed interface LogicStatement : Statement {
    data class IfElse(
        val condition: Expression,
        val ifBody: BlockStatement,
        val elseBody: BlockStatement,
    ) : LogicStatement {
        override fun toCode() = """
            if (${condition.toCode()}) {
                ${ifBody.toCode()}
            } else {
                ${elseBody.toCode()}
            }
        """.trimIndent()
    }
}