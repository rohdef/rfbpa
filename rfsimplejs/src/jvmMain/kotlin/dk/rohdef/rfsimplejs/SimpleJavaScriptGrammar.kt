package dk.rohdef.rfsimplejs

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast.*

open class SimpleJavaScript : AbstractGrammar<Expression>() {
    override fun start(): Rule<Expression> {
        return whitespaces() +
                expression() +
                eoi()
    }

    open fun expressions() =
        sequence(
            expression2(),
            push { ExpressionCollector(mutableListOf(pop(it))) },
            oneOrMore(
                sequence(
                    expression2(),
                    command { (peek(1, it) as ExpressionCollector).collection.add(pop(it)) }
                )
            ),
            push { Expressions((pop(it) as ExpressionCollector).collection.toList()) },
        )

    open fun expression() = choice(
        expressions(),
        expression2(),
    )

    open fun expression2() = sequence(
        choice(
            chainCall(),
            quoteString(),
            funcCall(),
            name(),
        ),
        zeroOrMore(semiColon()),
        whitespaces(),
    )

    // TODO deal with nested chains
    open fun chainCall() = sequence(
        choice(
            quoteString(),
            funcCall(),
            name(),
        ),
        period(),
        funcCall(),
        push {
            val f = pop(it) as FunctionCall
            val e = pop(it)
            ChainedCall(e, f)
        }
    )

    open fun name(): Rule<Expression> {
        return sequence(
            sequence(
                nameFirstChar(),
                zeroOrMore(nameChars()),
            ),
            push { Name(it.previousMatch.toString()) },
            whitespaces()
        )
    }

    open fun nameFirstChar(): Rule<Expression> {
        return choice(
            letter(),
            char('_'),
            char('$'),
        )
    }

    open fun nameChars(): Rule<Expression> {
        return choice(
            letterOrDigit(),
            char('_'),
            char('$'),
        )
    }

    open fun funcCall(): Rule<Expression> {
        return sequence(
            name(),
            parenthesisLeft(),
            // TODO deal with multiple parameters
            expression2(),
            parenthesisRight(),
            push {
                val contents = pop(it)
                val name = pop(it) as Name
                FunctionCall(
                    name,
                    contents,
                )
            },
        )
    }

    open fun quoteString(): Rule<Expression> {
        return sequence(
            singleQuote(),

            zeroOrMore(
                sequence(
                    testNot(singleQuote()),
                    anyChar(),
                ),
            ),
            push { Text(it.previousMatch.toString()) },

            singleQuoteEnd(),
        )
    }

    open fun doubleQuote() = char('"')
    open fun singleQuote() = char('\'')
    open fun singleQuoteEnd() = sequence(char('\''), whitespaces())
    open fun unaryMinus() = sequence(char('-'), whitespaces())

    open fun parenthesisLeft() = sequence(char('('), whitespaces())
    open fun parenthesisRight() = sequence(char(')'), whitespaces())
    open fun curlyBracketLeft() = sequence(char('{'), whitespaces())
    open fun curlyBracketRight() = sequence(char('}'), whitespaces())
    open fun squareBracketLeft() = sequence(char('['), whitespaces())
    open fun squareBracketRight() = sequence(char(']'), whitespaces())

    open fun colon() = sequence(char(':'), whitespaces())
    open fun semiColon() = sequence(char(';'), whitespaces())
    open fun period() = sequence(char('.'), whitespaces())
    open fun comma() = sequence(char(','), whitespaces())

    open fun whitespaces() = zeroOrMore(whitespace())
}