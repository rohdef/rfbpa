package dk.rohdef.rfsimplejs.ast2.statement

data class FunctionExpression(
    val params: List<Identifier>,
    val body: List<Statement>,
) : Expression {
    override fun toCode() = "function (${params.joinToString(", ") { it.toCode() }}) {\n${body.joinToString(separator = "\n") { it.toCode() }}\n}"
}
