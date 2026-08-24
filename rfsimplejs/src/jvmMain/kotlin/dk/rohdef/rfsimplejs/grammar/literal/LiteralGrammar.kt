package dk.rohdef.rfsimplejs.grammar.literal

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Null
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean

open class LiteralGrammar(
    private val javaScriptGrammar: JavaScriptGrammar = JavaScriptGrammar(),
    private val numberGrammar: NumberGrammar = NumberGrammar(),
    private val stringGrammar: StringGrammar = StringGrammar(),
) : AbstractGrammar<Statement>() {
    override fun start() = literal()

    open fun literal(): Rule<Statement> = choice(
        syntaticLiteral(),
        lexicalLiteral(),
//        hmm(),
    )

    open fun syntaticLiteral(): Rule<Statement> = array()

    open fun array(): Rule<Statement> = sequence(
        char('['),
        push { ArrayLiteral() },
        optional(
            sequence(
                zeroOrMore(whitespace()),
//                javaScriptGrammar.expression(),
//                push {
//                    val literal = pop(it) as Expression
//                    val array = pop(it) as ArrayLiteral
//                    array.withExpression(literal)
//                },
                zeroOrMore(
                    sequence(
                        zeroOrMore(whitespace()),
                        char(','),
                        zeroOrMore(whitespace()),
//                        literal(),
                        push {
                            val literal = pop(it) as Expression
                            val array = pop(it) as ArrayLiteral
                            array.withExpression(literal)
                        },
                    ),
                ),
                zeroOrMore(whitespace()),
                optional(
                    sequence(
                        char(','),
                        zeroOrMore(whitespace()),
                    ),
                ),
            ),
        ),
        char(']'),
    ) + push { pop(it) }

    open fun lexicalLiteral(): Rule<Statement> = choice(
        numberGrammar.numberLiteral(),
        stringGrammar.stringLiteral(),
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