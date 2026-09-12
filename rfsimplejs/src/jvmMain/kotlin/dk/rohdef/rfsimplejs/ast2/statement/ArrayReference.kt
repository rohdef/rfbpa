package dk.rohdef.rfsimplejs.ast2.statement

data class ArrayReference(
    val arrayExpression: Expression,
    val reference: Expression,
) : Expression {
    override fun toCode(): String {
        TODO("Not yet implemented")
    }
}
