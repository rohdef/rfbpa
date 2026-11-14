package dk.rohdef.rfsimplejs.ast2.statement.logic

import dk.rohdef.rfsimplejs.ast2.statement.BlockStatement
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement

data class Switch(
    val subject: Expression,
    val cases: List<SwitchCase> = emptyList(),
) : Statement {
    constructor(subject: Expression, vararg cases: SwitchCase) : this(subject, cases.toList())

    override fun toCode() = """
            switch (${subject.toCode()}) {
                ${cases.joinToString("\n    ") { it.toCode() }}
            }
        """.trimIndent()

    fun withCase(case: SwitchCase) = copy(cases = cases + case)
}

sealed interface SwitchCase : Statement {
    data class Default(
        val body: BlockStatement = BlockStatement(),
    ) : SwitchCase {
        constructor(vararg statements: Statement) : this(BlockStatement(*statements))

        override fun toCode() = "default:\n${body.toCode()}"
    }

    data class Match(
        val match: Expression,
        val body: BlockStatement = BlockStatement(),
    ) : SwitchCase {
        constructor(match: Expression, vararg statements: Statement) : this(match, BlockStatement(*statements))

        override fun toCode() = "case ${match.toCode()}:\n${body.toCode()}"
    }
}
