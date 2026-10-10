package dk.rohdef.rfsimplejs.ast2.statement

sealed interface Destructure : Expression {
    data class Object(
        val properties: Map<Identifier, Expression> = emptyMap(),
    ) : Destructure {
        override fun toCode(): String {
            TODO("Not yet implemented")
        }
    }

    data class Array(
        val elements: List<Pair<Identifier, Expression>>,
    ) : Destructure {
        override fun toCode(): String {
            TODO("Not yet implemented")
        }
    }
}