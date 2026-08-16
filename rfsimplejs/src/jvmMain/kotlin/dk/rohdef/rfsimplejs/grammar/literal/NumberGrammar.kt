package dk.rohdef.rfsimplejs.grammar.literal

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number

open class NumberGrammar : AbstractGrammar<Statement>() {
    override fun start(): Rule<Statement> = numberLiteral()

    open fun numberLiteral() = choice(
        scientificLiteral(),
        decimalLiteral(),
        binaryLiteral(),
        hexLiteral(),
        integerLiteral(),
    )

    open fun integerLiteral() = sequence(
        sequence(
            numberSign(),
            oneOrMore(digit()),
        ),
        push {
            Number.SimpleNumber.IntegerNumber(it.previousMatch.toString().toBigInteger())
        },
    )

    open fun decimalLiteral() = sequence(
        sequence(
            optional(numberSign()),
            choice(
                sequence(
                    oneOrMore(digit()),
                    char('.'),
                    zeroOrMore(digit()),
                ),
                sequence(
                    char('.'),
                    oneOrMore(digit()),
                ),
            ),
        ),
        push {
            Number.SimpleNumber.DecimalNumber(it.previousMatch.toString().toBigDecimal())
        },
    )

    open fun binaryLiteral() = sequence(
        sequence(
            numberSign(),
            string("0b"),
            regex("^[01]+$"),
        ),
        push {
            Number.BinaryNumber(it.previousMatch.toString())
        },
    )

    open fun hexLiteral() = sequence(
        sequence(
            numberSign(),
            string("0x"),
            regex("^[0-9a-fA-F]+$"),
        ),
        push {
            Number.HexadecimalNumber(it.previousMatch.toString())
        },
    )

    open fun scientificLiteral() = sequence(
        sequence(
            choice(
                decimalLiteral(),
                integerLiteral(),
            ),
            char('e'),
            integerLiteral(),
        ),
        push {
            val exponent = pop(it) as Number.SimpleNumber.IntegerNumber
            val coefficient = pop(it) as Number.SimpleNumber
            Number.ScientificNumber(coefficient, exponent)
        }
    )

    open fun numberSign() = optional(
        choice(
            char('+'),
            char('-'),
        )
    )
}