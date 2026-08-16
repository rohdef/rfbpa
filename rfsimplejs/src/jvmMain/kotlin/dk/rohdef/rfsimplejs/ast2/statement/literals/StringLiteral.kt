package dk.rohdef.rfsimplejs.ast2.statement.literals

data class StringLiteral(val value: String) : Literal {
    override fun toCode(): String = "\"$value\""
}