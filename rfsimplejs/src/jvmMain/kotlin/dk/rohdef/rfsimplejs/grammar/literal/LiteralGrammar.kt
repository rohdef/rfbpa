package dk.rohdef.rfsimplejs.grammar.literal

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean

open class LiteralGrammar(
    private val numberGrammar: NumberGrammar = NumberGrammar::class.createGrammar(),
    private val stringGrammar: StringGrammar = StringGrammar::class.createGrammar(),
) : AbstractGrammar<Statement>() {
    override fun start() = literal()

    open fun literal(): Rule<Statement> = lexicalLiteral()

    open fun lexicalLiteral(): Rule<Statement> = choice(
        numberGrammar.start(),
        stringGrammar.start(),
        nullLiteral(),
        booleanLiteral(),
    )

    open fun nullLiteral() = sequence(
        string("null"),
        push { Null },
    )

    open fun booleanLiteral() = choice(
        trueLiteral(),
        falseLiteral(),
    )

    open fun trueLiteral() = sequence(
        string("true"),
        push { AstBoolean.True },
    )

    open fun falseLiteral() = sequence(
        string("false"),
        push { AstBoolean.False },
    )
}