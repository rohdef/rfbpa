package dk.rohdef.rfsimplejs.ast2

import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier

sealed interface Declaration : Expression {
    val lvalue: Identifier
    val expression: Expression

    data class Variable(
        override val lvalue: Identifier,
        override val expression: Expression
    ) : Declaration {
        override fun toCode() = "var ${lvalue.toCode()} = ${expression.toCode()};"
    }

    data class Let(
        override val lvalue: Identifier,
        override val expression: Expression
    ) : Declaration {
        override fun toCode() = "let ${lvalue.toCode()} = ${expression.toCode()};"
    }

    data class Constant(
        override val lvalue: Identifier,
        override val expression: Expression
    ) : Declaration {
        override fun toCode() = "const ${lvalue.toCode()} = ${expression.toCode()};"
    }
}