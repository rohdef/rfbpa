package dk.rohdef.rfsimplejs.grammar.literal

import com.github.mpe85.grampa.grammar.AbstractGrammar
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean

open class LiteralGrammar : AbstractGrammar<Statement>() {
    private val numberGrammar = NumberGrammar()
    private val stringGrammar = StringGrammar()

    override fun start() = lexicalLiteral()

    open fun lexicalLiteral() = choice(
        stringGrammar.stringLiteral(),
        numberGrammar.numberLiteral(),
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