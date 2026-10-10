package dk.rohdef.rfsimplejs.ast2.statement.literals

import dk.rohdef.rfsimplejs.ast2.statement.Expression

object Undefined : Expression.PrimaryExpression {
    override fun toCode() = "undefined"
}