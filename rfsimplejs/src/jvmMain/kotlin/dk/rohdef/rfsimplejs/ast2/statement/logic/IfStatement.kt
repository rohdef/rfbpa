package dk.rohdef.rfsimplejs.ast2.statement.logic

import dk.rohdef.rfsimplejs.ast2.statement.BlockStatement
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class IfStatement(
    val condition: Expression,
    val action: Statement,
    val elseAction: Statement = BlockStatement(),
) : Statement {
    override fun toCode(): String {
        TODO("Not yet implemented")
    }
}