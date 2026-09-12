package dk.rohdef.rfsimplejs.ast2.statement

data class FunctionCall(
    val callee: Expression,
    val parameters: List<Expression>,
) : Expression {
    constructor(callee: Expression, vararg parameters: Expression) : this(callee, parameters.toList())

    fun withParameters(vararg newParameters: Expression) = FunctionCall(callee, parameters + newParameters.toList())

    override fun toCode() = "${callee.toCode()}(${parameters.joinToString(", ") { it.toCode() }})"
}