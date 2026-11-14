package dk.rohdef.rfsimplejs.ast2.statement.literals

import dk.rohdef.rfsimplejs.ast2.statement.Expression

data class TemplateLiteral(val values: List<TemplateLiteralPart> = emptyList()) : LexicalLiteral {
    constructor(vararg values: TemplateLiteralPart) : this(values.toList())

    override fun toCode(): String = "`${values.joinToString("") { it.toCode() }}`"

    fun withPart(part: TemplateLiteralPart) = copy(values = values + part)

    sealed interface TemplateLiteralPart {
        fun toCode(): String

        data class StringPart(val value: String) : TemplateLiteralPart {
            override fun toCode(): String = value
        }

        data class ExpressionPart(val expression: Expression) : TemplateLiteralPart {
            override fun toCode(): String = "\${${expression.toCode()}}"
        }
    }
}