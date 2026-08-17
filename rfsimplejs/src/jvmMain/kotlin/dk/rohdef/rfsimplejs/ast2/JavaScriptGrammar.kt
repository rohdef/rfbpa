package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.grammar.AbstractGrammar
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.grammar.DeclarationGrammar
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar

open class JavaScriptGrammar : AbstractGrammar<Statement>() {
    private val literalGrammar = LiteralGrammar()
    private val identifierGrammar = IdentifierGrammar()
    private val declarationGrammar = DeclarationGrammar(this)

    override fun start() = program() + eoi()

    open fun program() = sequence(
        zeroOrMore(ignorable()),
        topLevel(),
        push {
            val body = it.stack.toList()
            it.stack.clear()
            Program(body)
        }
    )

    open fun topLevel() = sequence(
        optional(statement()),
        zeroOrMore(
            sequence(
                codeSeparator(),
                zeroOrMore(ignorable()),
                statement(),
            )
        ),
    )

    open fun statement() = sequence(
        choice(
            declarationGrammar.declaration(),
            expressionStatement(),
        ),
        zeroOrMore(sameLineWhitespace()),
    )


    open fun expressionStatement() = expression()

    open fun expression() = primaryExpression()
//    open fun expression() = sequence(
//        choice(
//            primaryExpression()
//        ),
//        ignorable(),
//    )

    open fun primaryExpression() = choice(
        literalGrammar.lexicalLiteral(),
        identifierGrammar.identifier(),
    )

    open fun ignorable() = choice(
        semiColon(),
        whitespace(),
    )

    open fun sameLineWhitespace() = sequence(
        testNot(cr()),
        testNot(lf()),
        whitespace(),
    )

    open fun codeSeparator() = choice(
        semiColon(),
        lf(),
        cr(),
        crlf(),
    )

    open fun semiColon() = char(';')

    open fun unaryMinus() = char('-')


    open fun period() = char('.')

}