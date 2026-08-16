package dk.rohdef.rfsimplejs.ast2.statement

data class Identifier(
    val name: String,
) : Expression {
    override fun toCode() = name
}
