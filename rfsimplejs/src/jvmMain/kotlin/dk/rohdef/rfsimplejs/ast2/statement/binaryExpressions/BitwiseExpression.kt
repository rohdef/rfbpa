package dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions

import dk.rohdef.rfsimplejs.ast2.statement.Expression

sealed interface BitwiseExpression : BinaryExpression {
    data class And(
        val left: Expression,
        val right: Expression,
    ) : BitwiseExpression {
        override fun toCode() = "${left.toCode()} & ${right.toCode()}"
    }

    data class Or(
        val left: Expression,
        val right: Expression,
    ) : BitwiseExpression {
        override fun toCode() = "${left.toCode()} | ${right.toCode()}"
    }

    data class Xor(
        val left: Expression,
        val right: Expression,
    ) : BitwiseExpression {
        override fun toCode() = "${left.toCode()} ^ ${right.toCode()}"
    }

    data class LeftShift(
        val left: Expression,
        val right: Expression,
    ) : BitwiseExpression {
        override fun toCode() = "${left.toCode()} << ${right.toCode()}"
    }

    data class RightShift(
        val left: Expression,
        val right: Expression,
    ) : BitwiseExpression {
        override fun toCode() = "${left.toCode()} >> ${right.toCode()}"
    }

    data class UnsignedRightShift(
        val left: Expression,
        val right: Expression,
    ) : BitwiseExpression {
        override fun toCode() = "${left.toCode()} >>> ${right.toCode()}"
    }
}
