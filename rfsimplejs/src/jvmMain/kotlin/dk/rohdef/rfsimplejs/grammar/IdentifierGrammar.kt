package dk.rohdef.rfsimplejs.grammar

import com.github.mpe85.grampa.grammar.AbstractGrammar
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.Statement

open class IdentifierGrammar : AbstractGrammar<Statement>() {
    override fun start() = identifier()

    open fun identifier() = sequence(
        sequence(
            identifierFirstChar(),
            zeroOrMore(identifierChars()),
        ),
        push { Identifier(it.previousMatch!!.toString()) }
    )


    open fun identifierFirstChar() = choice(
        letter(),
        char('_'),
        char('$'),
    )

    open fun identifierChars() = choice(
        letterOrDigit(),
        char('_'),
        char('$'),
    )
}