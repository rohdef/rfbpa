package dk.rohdef.rfsimplejs.ast2.statement

data class ChainedCall(
    val left: Expression,
    val right: Expression,
) : Expression {
    override fun toCode() = "${left.toCode()}.${right.toCode()}"
}
