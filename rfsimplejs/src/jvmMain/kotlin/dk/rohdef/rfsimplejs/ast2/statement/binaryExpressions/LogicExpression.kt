package dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions

import dk.rohdef.rfsimplejs.ast2.statement.Expression

sealed interface LogicExpression : BinaryExpression {
    data class Equals(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} == ${right.toCode()}"
    }

    data class LessThan(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} < ${right.toCode()}"
    }

    data class GreaterThan(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} > ${right.toCode()}"
    }

    data class LessThanOrEqual(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} <= ${right.toCode()}"
    }

    data class GreaterThanOrEqual(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} >= ${right.toCode()}"
    }

    data class NonCoercingEquals(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} === ${right.toCode()}"
    }

    data class LogicalOr(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} || ${right.toCode()}"
    }

    data class LogicalAnd(
        val left: Expression,
        val right: Expression,
    ) : LogicExpression {
        override fun toCode() = "${left.toCode()} && ${right.toCode()}"
    }
}
