package dk.rohdef.rfsimplejs.ast2.statement

import dk.rohdef.rfsimplejs.ast2.statement.literals.Undefined

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

        fun withParameter(identifier: Identifier) = withParameter(Parameter.IdentifierParameter(identifier, Undefined))
        fun withParameter(identifier: Identifier, value: Expression) = withParameter(Parameter.IdentifierParameter(identifier, value))
        fun withParameter(parameter: Parameter) = copy(parameters = parameters + parameter)
    }

    sealed interface Parameter : Expression {
        data class IdentifierParameter(
            val identifier: Identifier,
            val value: Expression,
        ) : Parameter {
            constructor(identifier: String) : this(Identifier(identifier), Undefined)
            constructor(identifier: Identifier) : this(identifier, Undefined)
            constructor(identifier: String, expression: Expression) : this(Identifier(identifier), expression)

            override fun toCode() = "${identifier.toCode()} = ${value.toCode()}"
        }

        data class ObjectDestructure(
            val properties: List<Parameter> = emptyList(),
        ) : Parameter {
            constructor(vararg properties: Parameter) : this(properties.toList())

            override fun toCode(): String {
                TODO("Not yet implemented")
            }

            fun withProperty(property: Parameter) = copy(properties = properties + property)
        }

        data class ArrayDestructure(
            val elements: List<Parameter> = emptyList(),
        ) : Parameter {
            constructor(vararg elements: Parameter) : this(elements.toList())

            override fun toCode(): String {
                TODO("Not yet implemented")
            }

            fun withElement(element: Parameter) = copy(elements = elements + element)
        }
    }
}
