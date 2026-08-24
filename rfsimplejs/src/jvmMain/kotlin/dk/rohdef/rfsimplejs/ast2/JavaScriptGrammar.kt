package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.grammar.DeclarationGrammar
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.grammar.literal.NumberGrammar
import dk.rohdef.rfsimplejs.grammar.literal.StringGrammar

open class JavaScriptGrammar : AbstractGrammar<Statement>() {
    private val numberGrammar = NumberGrammar()
    private val stringGrammar = StringGrammar()
    private val identifierGrammar = IdentifierGrammar()
    private val declarationGrammar = DeclarationGrammar(
        this,
        identifierGrammar,
    )
    private val literalGrammar = LiteralGrammar(
        this,
        numberGrammar,
        stringGrammar,
    )

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
        syntaticLiteral(),
        identifierGrammar.identifier(),
    )

    open fun syntaticLiteral(): Rule<Statement> = array()

    open fun array() = sequence(
        char('['),
        push { ArrayLiteral() },
        optional(
            sequence(
                zeroOrMore(whitespace()),
                primaryExpression(),
                push {
                    val expression = pop(it) as Expression
                    val array = pop(it) as ArrayLiteral
                    array.withExpression(expression)
                },

                optional(
                    sequence(
                        zeroOrMore(whitespace()),
                        char(','),
                    ),
                ),
            ),
        ),

        zeroOrMore(whitespace()),
        char(']'),
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