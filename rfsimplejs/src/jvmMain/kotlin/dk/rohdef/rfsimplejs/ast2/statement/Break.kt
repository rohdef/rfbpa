package dk.rohdef.rfsimplejs.ast2.statement

data object Break : Statement {
    override fun toCode() = "break;"
}
