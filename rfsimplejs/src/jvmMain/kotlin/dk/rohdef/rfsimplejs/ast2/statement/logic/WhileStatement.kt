package dk.rohdef.rfsimplejs.ast2.statement.logic

import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class WhileStatement(
    val condition: Expression,
    val action: Statement,
) : Statement {
    override fun toCode(): String {
        TODO("Not yet implemented")
    }
}
