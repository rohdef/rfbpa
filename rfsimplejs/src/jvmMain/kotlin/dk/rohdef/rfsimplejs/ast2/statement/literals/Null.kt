package dk.rohdef.rfsimplejs.ast2.statement.literals

object Null : LexicalLiteral {
    override fun toCode() = "null"

    override fun toString() = "Null()"
}