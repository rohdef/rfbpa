package dk.rohdef.rfsimplejs.ast2.statement.literals

import dk.rohdef.rfsimplejs.ast2.statement.Expression

data class ObjectLiteral(
    val properties: Map<Expression, Expression>,
) : SyntaticLiteral {
    constructor(vararg properties: Pair<Expression, Expression>) : this(properties.toMap())

    override fun toCode() = """
        {
            ${properties.entries.joinToString(",\n") { (key, expression) -> "${key.toCode()}: ${expression.toCode()}" }}
        }
    """.trimIndent()

    fun withProperty(key: Expression, expression: Expression) = this.copy(properties = properties + (key to expression))
}
