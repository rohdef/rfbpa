package dk.rohdef.rfsimplejs.ast2.statement.literals

data class RegexLiteral(
    val value: String,
) : LexicalLiteral {
    override fun toCode(): String = "/$value/"
}
