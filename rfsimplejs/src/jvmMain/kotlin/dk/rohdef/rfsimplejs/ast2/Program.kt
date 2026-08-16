package dk.rohdef.rfsimplejs.ast2

import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class Program(
    val body: List<Statement>,
) : Statement {
    constructor(vararg statements: Statement) : this(listOf(*statements))

    override fun toCode() =
        body.joinToString(separator = "\n") { it.toCode() }
}
