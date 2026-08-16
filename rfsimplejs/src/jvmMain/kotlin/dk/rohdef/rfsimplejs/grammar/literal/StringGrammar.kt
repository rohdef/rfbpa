package dk.rohdef.rfsimplejs.grammar.literal

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral

open class StringGrammar : AbstractGrammar<Statement>() {
    override fun start() = stringLiteral()

    open fun stringLiteral() = choice(
        doubleQuoteString(),
        singleQuoteString(),
    )

    open fun doubleQuoteString() = quotedString(doubleQuote())

    open fun singleQuoteString() = quotedString(singleQuote())

    open fun quotedString(quote: Rule<Statement>) = sequence(
        quote,
        zeroOrMore(
            choice(
                char('\\') + quote,
                sequence(
                    testNot(quote),
                    anyChar(),
                ),
            ),
        ),
        push {
            StringLiteral(it.previousMatch!!.toString())
        },
        quote,
    )

    open fun doubleQuote() = char('"')
    open fun singleQuote() = char('\'')
}