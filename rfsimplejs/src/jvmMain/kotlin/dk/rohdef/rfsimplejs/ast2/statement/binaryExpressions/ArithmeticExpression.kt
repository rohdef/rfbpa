package dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions

import dk.rohdef.rfsimplejs.ast2.statement.Expression

sealed interface ArithmeticExpression : BinaryExpression {
    data class Addition(
        val left: Expression,
        val right: Expression,
    ) : ArithmeticExpression {
        override fun toCode() = "${left.toCode()} + ${right.toCode()}"
    }

    data class Subtraction(
        val left: Expression,
        val right: Expression,
    ) : ArithmeticExpression {
        override fun toCode() = "${left.toCode()} - ${right.toCode()}"
    }

    data class Multiplication(
        val left: Expression,
        val right: Expression,
    ) : ArithmeticExpression {
        override fun toCode() = "${left.toCode()} * ${right.toCode()}"
    }

    data class Division(
        val left: Expression,
        val right: Expression,
    ) : ArithmeticExpression {
        override fun toCode() = "${left.toCode()} / ${right.toCode()}"
    }

    data class Modulo(
        val left: Expression,
        val right: Expression,
    ) : ArithmeticExpression {
        override fun toCode() = "${left.toCode()} % ${right.toCode()}"
    }
}