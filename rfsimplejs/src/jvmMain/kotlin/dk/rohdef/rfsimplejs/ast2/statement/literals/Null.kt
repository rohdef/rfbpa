package dk.rohdef.rfsimplejs.ast2.statement.literals

object Null : Literal {
    override fun toCode() = "null"

    override fun toString() = "Null()"
}