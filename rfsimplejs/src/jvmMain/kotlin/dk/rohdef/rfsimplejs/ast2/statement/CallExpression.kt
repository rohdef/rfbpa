package dk.rohdef.rfsimplejs.ast2.statement

data class CallExpression(
    val callee: Expression,
) : Expression {
    override fun toCode() = "${callee.toCode()}()"
}