package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.Addition
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.grammar.literal.LiteralGrammar
import dk.rohdef.rfsimplejs.grammar.literal.NumberGrammar
import dk.rohdef.rfsimplejs.grammar.literal.StringGrammar

open class JavaScriptGrammar : AbstractGrammar<Statement>() {
    private val numberGrammar = NumberGrammar::class.createGrammar()
    private val stringGrammar = StringGrammar::class.createGrammar()
    private val identifierGrammar = IdentifierGrammar::class.createGrammar()
    private val literalGrammar = LiteralGrammar::class.createGrammar(
        numberGrammar,
        stringGrammar,
    )

    override fun start() = program() + eoi()

    open fun program() = sequence(
        zeroOrMore(ignorable()),
        topLevel(),
        push {
            val body = it.stack.toList().reversed()
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
            ),
        ),
        optional(codeSeparator()),
    )

    open fun statement() = sequence(
        choice(
            declaration(),
            expressionStatement(),
        ),
        zeroOrMore(sameLineWhitespace()),
    )


    open fun expressionStatement() = expression()

    open fun expression() = choice(
        binaryExpression(),
        primaryExpression(),
    )

    open fun binaryExpression() = arithmetic()

    open fun arithmetic() = addition()

    open fun addition(): Rule<Statement> = sequence(
        primaryExpression(),
        zeroOrMore(sameLineWhitespace()),
        char('+'),
        zeroOrMore(whitespace()),
        expression(),
        zeroOrMore(sameLineWhitespace()),
        push {
            val right = pop(it) as Expression
            val left = pop(it) as Expression
            Addition(left, right)
        }
    )

    open fun primaryExpression() = choice(
        literalGrammar.start(),
        identifierGrammar.start(),
        syntaticLiteral(),
    )

    open fun syntaticLiteral(): Rule<Statement> = choice(
        objectLiteral(),
        array(),
    )

    open fun objectLiteral() = sequence(
        char('{'),
        push { ObjectLiteral() },
        optional(
            sequence(
                objectProperty(),
                zeroOrMore(
                    sequence(
                        zeroOrMore(whitespace()),
                        char(','),
                        objectProperty(),
                    ),
                ),
                zeroOrMore(whitespace()),
                optional(
                    sequence(
                        char(','),
                        zeroOrMore(whitespace()),
                    ),
                ),
            )
        ),
        char('}'),
    )

    open fun objectProperty() = sequence(
        zeroOrMore(whitespace()),
        identifierGrammar.start(),
        zeroOrMore(whitespace()),
        char(':'),
        zeroOrMore(whitespace()),
        primaryExpression(),
        push {
            val expression = pop(it) as Expression
            val identifier = pop(it) as Identifier
            val objectLiteral = pop(it) as ObjectLiteral
            objectLiteral.withProperty(identifier, expression)
        },
    )

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
                zeroOrMore(
                    sequence(
                        zeroOrMore(whitespace()),
                        char(','),
                        zeroOrMore(whitespace()),
                        primaryExpression(),
                        push {
                            val literal = pop(it) as Expression
                            val array = pop(it) as ArrayLiteral
                            array.withExpression(literal)
                        },
                    ),
                ),
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

    open fun declaration() = choice(
        variable(),
        constant(),
        let(),
    )

    open fun variable() = sequence(
        sequence(
            string("var"),
            oneOrMore(sameLineWhitespace()),
            push { Declaration.Variable() },
            lvalueExpressions(),
        ),
    )

    open fun constant() = sequence(
        sequence(
            string("const"),
            oneOrMore(sameLineWhitespace()),
            push { Declaration.Constant() },
            lvalueExpressions(),
        ),
    )

    open fun let() = sequence(
        sequence(
            string("let"),
            oneOrMore(sameLineWhitespace()),
            push { Declaration.Let() },
            lvalueExpressions(),
        ),
    )

    open fun lvalueExpressions() = sequence(
        lvalueExpression(),
        zeroOrMore(
            sequence(
                zeroOrMore(sameLineWhitespace()),
                char(','),
                zeroOrMore(whitespace()),
                lvalueExpression(),
            )
        ),
    )

    open fun lvalueExpression() = sequence(
        sequence(
            identifierGrammar.start(),
            zeroOrMore(sameLineWhitespace()),
            char('='),
            zeroOrMore(whitespace()),
            expression(),
            zeroOrMore(sameLineWhitespace()),
        ),
        push {
            val expression = pop(it) as Expression
            val lvalue = pop(it) as Identifier
            val declaration = pop(it) as Declaration<*>
            declaration.withDeclarion(lvalue, expression)
        },
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
}