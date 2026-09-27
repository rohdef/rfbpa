package dk.rohdef.rfsimplejs.ast2.statement.logic

import dk.rohdef.rfsimplejs.ast2.statement.Expression

data class TernaryExpression(
    val condition: Expression,
    val ifBody: Expression,
    val elseBody: Expression,
) : Expression {
    override fun toCode() = """
            ${condition.toCode()} ? ifBody.toCode() : ${elseBody.toCode()}
        """.trimIndent()
}