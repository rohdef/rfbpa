package dk.rohdef.rfsimplejs.grammar.literal

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.RegexLiteral

open class RegexGrammar : AbstractGrammar<Statement>() {
    override fun start(): Rule<Statement> = regexLiteral()

    open fun regexLiteral(): Rule<Statement> = sequence(
        char('/'),
        oneOrMore(regexBodyChar()),
        push {
            RegexLiteral(it.previousMatch!!.toString())
        },
        char('/'),
        zeroOrMore(regexFlag()),
        push {
            val flags = it.previousMatch!!.toString()
            val regexLiteral = pop(it) as RegexLiteral
            RegexLiteral(regexLiteral.value, flags)
        },
    )

    open fun regexBodyChar() = choice(
        sequence(
            char('\\'),
            anyChar(),
        ),
        regexCharacterClass(),
        sequence(
            testNot(
                choice(
                    char('/'),
                    char('\n'),
                    char('\r'),
                ),
            ),
            anyChar(),
        ),
    )

    open fun regexCharacterClass() = sequence(
        char('['),
        zeroOrMore(
            choice(
                sequence(
                    char('\\'),
                    anyChar(),
                ),
                sequence(
                    testNot(
                        choice(
                            char(']'),
                            char('\n'),
                            char('\r'),
                        ),
                    ),
                    anyChar(),
                ),
            ),
        ),
        char(']'),
    )

    open fun regexFlag() = choice(
        charRange('a', 'z'),
        charRange('A', 'Z'),
    )
}
