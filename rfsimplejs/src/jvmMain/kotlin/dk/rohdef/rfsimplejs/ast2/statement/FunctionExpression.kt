package dk.rohdef.rfsimplejs.ast2.statement

data class FunctionExpression(
    val params: List<Identifier>,
    val body: List<Statement>,
) : Expression
