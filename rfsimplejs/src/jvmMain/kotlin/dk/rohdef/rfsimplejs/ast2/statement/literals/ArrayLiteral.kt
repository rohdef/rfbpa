package dk.rohdef.rfsimplejs.ast2.statement.literals

import dk.rohdef.rfsimplejs.ast2.statement.Expression

data class ArrayLiteral(
    val expressions: List<Expression>
) : SyntaticLiteral {
    constructor(vararg expressions: Expression) : this(expressions.toList())

    fun withExpression(expression: Expression): ArrayLiteral {
        return ArrayLiteral(expressions + expression)
    }

    override fun toCode() = "[${expressions.joinToString(", ")}]"
}
