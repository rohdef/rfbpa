package dk.rohdef.rfsimplejs.ast2.statement

data class EnclosedExpression(
    val expression: Expression,
) : Expression {
    override fun toCode() = "(${expression.toCode()})"
}
