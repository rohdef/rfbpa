package dk.rohdef.rfsimplejs.ast2.statement.literals

data class RegexLiteral(
    val value: String,
    val flags: String = "",
) : LexicalLiteral {
    override fun toCode(): String = "/$value/$flags"
}
