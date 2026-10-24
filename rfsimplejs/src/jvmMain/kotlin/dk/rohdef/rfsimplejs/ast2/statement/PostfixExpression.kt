package dk.rohdef.rfsimplejs.ast2.statement

sealed interface PostfixExpression : Expression {
    data class Increment(val expression: Expression) : PrefixExpression {
        override fun toCode() = "${expression.toCode()}++"
    }

    data class Decrement(val expression: Expression) : PrefixExpression {
        override fun toCode() = "${expression.toCode()}--"
    }
}