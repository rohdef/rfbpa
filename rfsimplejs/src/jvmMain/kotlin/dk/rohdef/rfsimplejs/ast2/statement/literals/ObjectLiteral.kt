package dk.rohdef.rfsimplejs.ast2.statement.literals

import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class ObjectLiteral(
    val properties: Map<Identifier, Expression>,
) : SyntaticLiteral {
    constructor(vararg properties: Pair<Identifier, Expression>) : this(properties.toMap())

    override fun toCode() = """
        {
            ${properties.entries.joinToString(",\n") { (identifier, expression) -> "${identifier.toCode()}: ${expression.toCode()}" }}
        }
    """.trimIndent()

    fun withProperty(identifier: Identifier, expression: Expression) = this.copy(properties = properties + (identifier to expression))
}
