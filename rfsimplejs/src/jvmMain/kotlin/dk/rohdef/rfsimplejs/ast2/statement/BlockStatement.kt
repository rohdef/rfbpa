package dk.rohdef.rfsimplejs.ast2.statement

data class BlockStatement(
    val statements: List<Statement>,
) : Statement {
    constructor(vararg statements: Statement) : this(statements.toList())

    override fun toCode() = statements.joinToString(";\n") { it.toCode() }

    fun withStatement(statement: Statement) = copy(statements = statements + statement)
}
