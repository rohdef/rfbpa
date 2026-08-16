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
        ignorable(),
        zeroOrMore(
            topLevel()
        ),
        push {
            Program(it.stack.toList())
        }
    )

    open fun topLevel() = statement()
//    open fun topLevel() = choice(
//        statement(),
//        expression(),
//    )

    open fun statement() = choice(
        declarationGrammar.declaration(),
        expressionStatement(),
    )

    open fun lvalue() = identifierGrammar.identifier()
//    open fun lvalue() = choice(
//        identifier(),
//        destructureTarget(),
//    )


    open fun expressionStatement() = sequence(
        expression(),
        optional(semiColon()),
    )

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

    open fun ignorable() = whitespaces()

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