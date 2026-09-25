package dk.rohdef.rfsimplejs.ast2.statement

data class FunctionExpression(
    val parameters: Parameters,
    val body: BlockStatement,
) : Expression {
    override fun toCode() = "function (${parameters.toCode()}) {\n${body.toCode() }}\n}"

    data class Parameters(
        val parameters: List<Parameter>,
    ) : Statement {
        override fun toCode() = parameters.joinToString(", ") { it.toCode() }

        constructor(vararg parameters: Parameter) : this(parameters.toList())

        fun withParameter(identifier: Identifier) = copy(parameters = parameters + Parameter.IdentifierParameter(identifier))
        fun withParameter(identifier: Identifier, value: Expression) = copy(parameters = parameters + Parameter.DefaultValue(identifier, value))
    }

    sealed interface Parameter {
        fun toCode(): String

        data class IdentifierParameter(val identifier: Identifier) : Parameter {
            override fun toCode() = identifier.toCode()
        }

        data class DefaultValue(
            val identifier: Identifier,
            val value: Expression,
        ) : Parameter {
            override fun toCode() = "${identifier.toCode()} = ${value.toCode()}"
        }
    }
}
