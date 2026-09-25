package dk.rohdef.rfsimplejs.ast2.statement

import dk.rohdef.rfsimplejs.ast2.statement.Expression as ExpressionType

sealed interface Return : ExpressionType {
    object Void : Return {
        override fun toCode() = "return;"
    }

    data class Expression(val expression: ExpressionType) : Return {
        override fun toCode() = "return ${expression.toCode()};"
    }
}