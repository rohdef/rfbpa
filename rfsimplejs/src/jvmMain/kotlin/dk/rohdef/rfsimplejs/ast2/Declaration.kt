package dk.rohdef.rfsimplejs.ast2

import dk.rohdef.rfsimplejs.ast2.Declaration.Variable
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier

sealed interface Declaration<T : Declaration<T>> : Expression {
    val declarations: Map<Identifier, Expression>

    fun declarationsAsCode(): String {
        return declarations
            .map { "${it.key.toCode()} = ${it.value.toCode()}" }
            .joinToString(",\n    ")
    }

    fun withDeclarion(identifier: Identifier, expression: Expression): T

    data class Variable(
        override val declarations: Map<Identifier, Expression> = emptyMap(),
    ) : Declaration<Variable> {
        constructor(vararg declarations: Pair<Identifier, Expression>) : this(declarations.toMap())

        override fun toCode() = "var ${declarationsAsCode()};"

        override fun withDeclarion(identifier: Identifier, expression: Expression) = copy(declarations = declarations + (identifier to expression))
    }

    data class Let(
        override val declarations: Map<Identifier, Expression> = emptyMap(),
    ) : Declaration<Let> {
        constructor(vararg declarations: Pair<Identifier, Expression>) : this(declarations.toMap())

        override fun toCode() = "let ${declarationsAsCode()};"

        override fun withDeclarion(identifier: Identifier, expression: Expression) = copy(declarations = declarations + (identifier to expression))
    }

    data class Constant(
        override val declarations: Map<Identifier, Expression> = emptyMap(),
    ) : Declaration<Constant> {
        constructor(vararg declarations: Pair<Identifier, Expression>) : this(declarations.toMap())

        override fun toCode() = "const ${declarationsAsCode()};"

        override fun withDeclarion(identifier: Identifier, expression: Expression) = copy(declarations = declarations + (identifier to expression))
    }
}