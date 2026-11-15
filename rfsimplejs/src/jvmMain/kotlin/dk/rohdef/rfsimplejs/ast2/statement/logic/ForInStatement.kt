package dk.rohdef.rfsimplejs.ast2.statement.logic

import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class ForInStatement(
    val variable: Statement,
    val expression: Expression,
    val action: Statement,
) : Statement {
    override fun toCode() = """
        for (${variable.toCode()} in ${expression.toCode()}) {
            ${action.toCode()}
        }
    """.trimIndent()
}
