package dk.rohdef.rfsimplejs.ast2.statement

data class Assignment(
    val lvalue: Identifier,
    val expression: Expression,
) : Expression {
    override fun toCode() = "${lvalue.toCode()} = ${expression.toCode()}"
}