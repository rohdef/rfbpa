package dk.rohdef.rfsimplejs.ast2.statement

data object Continue : Statement {
    override fun toCode() = "continue;"
}
