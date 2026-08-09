package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.Statement

open class SimpleJavaScript2 : AbstractGrammar<Statement>() {
    override fun start(): Rule<Statement> {
        return program() +
                eoi()
    }

    open fun program() =
        ignorable() +
                zeroOrMore(lexicalLiteral()) +
                push {
                    Program(it.stack.toList())
                }

    open fun topLevel() = choice(
        statement(),
//        functionDeclaration(),
//        classDeclaration(),
    )

    open fun functionDeclaration() = choice(

    )

    open fun classDeclaration() = choice(

    )

    open fun statement() = choice(
        expressionStatement()
    )

    open fun expressionStatement() = sequence(
        expression(),
        zeroOrMore(semiColon()),
    )

    open fun expression() = sequence(
        choice(
            primaryExpression()
        ),
        ignorable(),
    )

    open fun primaryExpression() = choice(
        lexicalLiteral(),
    )

    open fun lexicalLiteral() = choice(
        exponentialLiteral(),
        binaryLiteral(),
        hexLiteral(),
        decimalLiteral(),
        integerLiteral(),
    )

    open fun integerLiteral() = sequence(
        numberSign(),
        oneOrMore(digit()),
        push {
            val i = it.previousMatch.toString()
            println("Integer literal: $i")
            Number.SimpleNumber.IntegerNumber(i.toBigInteger())
        },
    )

    open fun decimalLiteral() = sequence(
        numberSign(),
        zeroOrMore(digit()),
        char('.'),
        oneOrMore(digit()),
        push {
            Number.SimpleNumber.DecimalNumber(it.previousMatch.toString().toBigDecimal())
        },
    )

    open fun hexLiteral() = sequence(
        numberSign(),
        char('0'),
        char('x'),
        regex("^[0-9a-fA-F]+$"),
        push {
            Number.HexadecimalNumber(it.previousMatch.toString())
        },
    )

    open fun binaryLiteral() = sequence(
        numberSign(),
        char('0'),
        char('b'),
        regex("^[01]+$"),
        push {
            Number.BinaryNumber(it.previousMatch.toString())
        },
    )

    open fun exponentialLiteral() = sequence(
        integerLiteral(),
        char('e'),
        integerLiteral(),
        push {
            val coeffient = pop(it) as Number.SimpleNumber
            val exponent = pop(it) as Number.SimpleNumber.IntegerNumber
            Number.ScientificNumber(coeffient, exponent)
        }
    )

    open fun numberSign() = optional(
        choice(
            char('+'),
            char('-'),
        )
    )

    open fun ignorable() = whitespaces()

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