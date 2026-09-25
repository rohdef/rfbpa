package dk.rohdef.rfsimplejs.ast2.statement

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

    data class TypeOf(val expression: Expression) : PrefixExpression {
        override fun toCode() = "typeof ${expression.toCode()}"
    }

    data class Delete(val expression: Expression) : PrefixExpression {
        override fun toCode() = "delete ${expression.toCode()}"
    }

    data class New(val expression: Expression) : PrefixExpression {
        override fun toCode() = "new ${expression.toCode()}"
    }

    data class Void(val expression: Expression) : PrefixExpression {
        override fun toCode() = "void ${expression.toCode()}"
    }

    data class Await(val expression: Expression) : PrefixExpression {
        override fun toCode() = "await ${expression.toCode()}"
    }
}