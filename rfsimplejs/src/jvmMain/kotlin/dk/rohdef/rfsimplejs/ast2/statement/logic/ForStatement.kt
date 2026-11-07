package dk.rohdef.rfsimplejs.ast2.statement.logic

import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class ForStatement(
    val initialization: Statement,
    val condition: Expression,
    val update: Statement,
    val action: Statement,
) : Statement {
    override fun toCode() = """
        for (${initialization.toCode()}; ${condition.toCode()}; ${update.toCode()}) {
            ${action.toCode()}
        }
    """.trimIndent()
}
