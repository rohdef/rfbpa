package dk.rohdef.rfsimplejs.ast2.statement.literals

import dk.rohdef.rfsimplejs.ast2.statement.Expression

data class TemplateLiteral(val values: List<TemplateLiteralPart>) : LexicalLiteral {
    override fun toCode(): String = "`${values.joinToString { it.toCode() }}`"

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