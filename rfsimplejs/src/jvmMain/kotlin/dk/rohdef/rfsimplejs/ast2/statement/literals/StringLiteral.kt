package dk.rohdef.rfsimplejs.ast2.statement.literals

data class StringLiteral(val value: String) : LexicalLiteral {
    override fun toCode(): String = "\"$value\""
}