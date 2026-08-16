package dk.rohdef.rfsimplejs.ast2.statement.literals

sealed interface Boolean : Literal {
    object True: Boolean {
        override fun toCode() = "true"

        override fun toString() = "True()"
    }

    object False: Boolean {
        override fun toCode() = "false"

        override fun toString() = "False()"
    }
}