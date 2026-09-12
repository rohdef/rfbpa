package dk.rohdef.rfsimplejs.ast2

import dk.rohdef.rfsimplejs.ast2.statement.Expression

sealed interface PrefixExpression : Expression {
    data class Not(val expression: Expression) : PrefixExpression {
        override fun toCode() = "!${expression.toCode()}"
    }

    data class Plus(val expression: Expression) : PrefixExpression {
        override fun toCode() = "+${expression.toCode()}"
    }

    data class Minus(val expression: Expression) : PrefixExpression {
        override fun toCode() = "-${expression.toCode()}"
    }

    data class Increment(val expression: Expression) : PrefixExpression {
        override fun toCode() = "++${expression.toCode()}"
    }

    data class Decrement(val expression: Expression) : PrefixExpression {
        override fun toCode() = "--${expression.toCode()}"
    }
}