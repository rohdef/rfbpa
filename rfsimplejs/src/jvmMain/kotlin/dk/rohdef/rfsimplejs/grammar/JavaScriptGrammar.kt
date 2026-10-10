package dk.rohdef.rfsimplejs.grammar

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.Declaration
import dk.rohdef.rfsimplejs.ast2.Program
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.logic.TernaryExpression
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

    override fun start(): Rule<Statement> = program() + eoi()

    open fun program(): Rule<Statement> = sequence(
        zeroOrMore(ignorable()),
        push { BlockStatement() },
        topLevel(),
        push {
            require(it.stack.size == 1)
            val blockStatement = pop(it) as BlockStatement
            Program(blockStatement.statements)
        }
    )

    open fun topLevel(): Rule<Statement> = sequence(
        optional(
            sequence(
                topLevelItem(),
                zeroOrMore(
                    sequence(
                        codeSeparator(),
                        zeroOrMore(ignorable()),
                        topLevelItem(),
                    ),
                ),
                optional(semiColon),
            ),
        ),
        zeroOrMore(ignorable()),
    )

    open fun topLevelItem(): Rule<Statement> = sequence(
        choice(
            functionDeclaration(),
            statement(),
        ),

        push {
            val statement = pop(it)
            val blockStatment = pop(it) as BlockStatement

            blockStatment.withStatement(statement)
        },
    )

    open fun statement(): Rule<Statement> = sequence(
        choice(
            declaration(),
            expression(),
        ),
        zeroOrMore(sameLineWhitespace()),
    )

    open fun functionDeclaration(): Rule<Statement> = sequence(
        string("function"),
        oneOrMore(whitespace()),
        identifierGrammar.start(),
        zeroOrMore(whitespace()),

        functionParameters(),
        blockStatement(),

        push {
            val blockStatement = pop(it) as BlockStatement
            val parameters = pop(it) as FunctionExpression.Parameters
            val identifier = pop(it) as Identifier

            Declaration.Variable(
                identifier to FunctionExpression(
                    parameters,
                    blockStatement,
                )
            )
        }
    )

    open fun functionExpression(): Rule<Statement> = sequence(
        string("function"),
        zeroOrMore(whitespace()),

        functionParameters(),
        blockStatement(),

        push {
            val blockStatement = pop(it) as BlockStatement
            val parameters = pop(it) as FunctionExpression.Parameters

            FunctionExpression(
                parameters,
                blockStatement,
            )
        }
    )

    open fun functionParameters(): Rule<Statement> = sequence(
        leftParenthesis,
        push { FunctionExpression.Parameters() },
        zeroOrMore(whitespace()),
        optional(
            sequence(
                functionParameter(),

                zeroOrMore(
                    sequence(
                        comma,
                        zeroOrMore(whitespace()),
                        functionParameter(),
                    )
                ),
            )
        ),
        rightParenthesis,
        zeroOrMore(whitespace()),
    )

    open fun functionParameter(): Rule<Statement> = choice(
        functionArrayDestructureParameter(),
        functionObjectDestructureParameter(),
        functionIdentifierParameter(),
    )

    open fun functionIdentifierParameter(): Rule<Statement> = sequence(
        identifierGrammar.start(),
        zeroOrMore(whitespace()),
        choice(
            sequence(
                equalChar,
                zeroOrMore(whitespace()),
                expression(),
                zeroOrMore(whitespace()),

                push {
                    val expression = pop(it) as Expression
                    val identifier = pop(it) as Identifier
                    val parameters = pop(it) as FunctionExpression.Parameters

                    parameters.withParameter(identifier, expression)
                },
            ),

            push {
                val identifier = pop(it) as Identifier
                val parameters = pop(it) as FunctionExpression.Parameters

                parameters.withParameter(identifier)
            },
        ),
    )

    open fun functionArrayDestructureParameter(): Rule<Statement> = sequence(
        functionArrayDestructureParameter2(),

        push {
            val parameter = pop(it) as FunctionExpression.Parameter.ArrayDestructure
            val parameters = pop(it) as FunctionExpression.Parameters

            parameters.withParameter(parameter)
        },
    )

    open fun functionArrayDestructureParameter2(): Rule<Statement> = sequence(
        leftSquareBracket,

        push { FunctionExpression.Parameter.ArrayDestructure() },

        zeroOrMore(whitespace()),
        optional(arrayDestructureElements()),

        rightSquareBracket,
    )

    open fun arrayDestructureElements(): Rule<Statement> = sequence(
        arrayDestructureElement(),
        zeroOrMore(
            sequence(
                comma,
                zeroOrMore(whitespace()),
                arrayDestructureElement()
            ),
        ),
    )

    open fun arrayDestructureElement(): Rule<Statement> = sequence(
        identifierGrammar.start(),
        zeroOrMore(whitespace()),

        choice(
            sequence(
                equalChar,
                zeroOrMore(whitespace()),
                choice(
                    functionArrayDestructureParameter2(),
                    functionObjectDestructureParameter2(),
                    primaryExpression(),
                ),
                zeroOrMore(whitespace()),

                push {
                    val value = pop(it) as Expression
                    val element = pop(it) as Identifier
                    val parameter = pop(it) as FunctionExpression.Parameter.ArrayDestructure

                    parameter.withElement(FunctionExpression.Parameter.IdentifierParameter(element, value))
                },
            ),

            push {
                val element = pop(it) as Identifier
                val parameter = pop(it) as FunctionExpression.Parameter.ArrayDestructure

                parameter.withElement(FunctionExpression.Parameter.IdentifierParameter(element))
            },
        ),
    )

    open fun functionObjectDestructureParameter(): Rule<Statement> = sequence(
        functionObjectDestructureParameter2(),

        push {
            val parameter = pop(it) as FunctionExpression.Parameter.ObjectDestructure
            val parameters = pop(it) as FunctionExpression.Parameters

            parameters.withParameter(parameter)
        },
    )

    open fun functionObjectDestructureParameter2(): Rule<Statement> = sequence(
        leftCurlyBracket,

        push { FunctionExpression.Parameter.ObjectDestructure() },

        zeroOrMore(whitespace()),
        optional(objectDestructureElements()),

        rightCurlyBracket,
    )

    open fun objectDestructureElements(): Rule<Statement> = sequence(
        objectDestructureElement(),
        zeroOrMore(
            sequence(
                comma,
                zeroOrMore(whitespace()),
                objectDestructureElement()
            ),
        ),
    )

    open fun objectDestructureElement(): Rule<Statement> = sequence(
        identifierGrammar.start(),
        zeroOrMore(whitespace()),
        choice(
            sequence(
                equalChar,
                zeroOrMore(whitespace()),
                choice(
                    functionArrayDestructureParameter2(),
                    functionObjectDestructureParameter2(),
                    primaryExpression(),
                ),
                zeroOrMore(whitespace()),

                push {
                    val value = pop(it) as Expression
                    val element = pop(it) as Identifier
                    val parameter = pop(it) as FunctionExpression.Parameter.ObjectDestructure

                    parameter.withProperty(FunctionExpression.Parameter.IdentifierParameter(element, value))
                }
            ),

            push {
                val element = pop(it) as Identifier
                val parameter = pop(it) as FunctionExpression.Parameter.ObjectDestructure

                parameter.withProperty(FunctionExpression.Parameter.IdentifierParameter(element))
            },
        ),
    )

    open fun blockStatement(): Rule<Statement> = sequence(
        push { BlockStatement() },
        leftCurlyBracket,
        zeroOrMore(ignorable()),
        topLevel(),
        rightCurlyBracket,
    )

    open fun expression(): Rule<Statement> = sequence(
        choice(
            keywordPrefixFactory(string("return ")) { expression -> Return.Expression(expression) },
            sequence(
                string("return"),
                push { Return.Void },
            ),
            functionExpression(),
            primaryExpression(),
        ),
        zeroOrMore(
            choice(
                ternaryExpression(),
                binaryExpression(),
            ),
        ),
    )

    open fun primaryExpression(): Rule<Statement> = sequence(
        choice(
            enclosedExpression(),
            keywordPrefixExpression(),
            literal(),
            prefixExpression(),
        ),
        zeroOrMore(continuation()),
        zeroOrMore(precedenceArithmetic()),
    )

    open fun enclosedExpression(): Rule<Statement> = sequence(
        leftParenthesis,
        zeroOrMore(whitespace()),
        expression(),
        zeroOrMore(whitespace()),
        rightParenthesis,

        push {
            val expression = pop(it) as Expression
            EnclosedExpression(expression)
        }
    )

    open fun literal() = choice(
        literalGrammar.start(),
        identifierGrammar.start(),
        syntaticLiteral(),
    )

    open fun keywordPrefixExpression(): Rule<Statement> = choice(
        keywordPrefixFactory(string("typeof ")) { expression -> PrefixExpression.TypeOf(expression) },
        keywordPrefixFactory(string("delete ")) { expression -> PrefixExpression.Delete(expression) },
        keywordPrefixFactory(string("new ")) { expression -> PrefixExpression.New(expression) },
        keywordPrefixFactory(string("void ")) { expression -> PrefixExpression.Void(expression) },
        keywordPrefixFactory(string("await ")) { expression -> PrefixExpression.Await(expression) },
    )

    open fun prefixExpression(): Rule<Statement> = choice(
        prefixFactory(string("++")) { expression -> PrefixExpression.Increment(expression) },
        prefixFactory(string("--")) { expression -> PrefixExpression.Decrement(expression) },
        prefixFactory(exclamation) { expression -> PrefixExpression.Not(expression) },
        prefixFactory(plus) { expression -> PrefixExpression.Plus(expression) },
        prefixFactory(minus) { expression -> PrefixExpression.Minus(expression) },
    )

    open fun keywordPrefixFactory(
        matcher: Rule<Statement>,
        prefixExpression: (Expression) -> Expression
    ): Rule<Statement> =
        sequence(
            matcher,
            zeroOrMore(whitespace()),
            expression(),
            push {
                val expression = pop(it) as Expression
                prefixExpression(expression)
            }
        )

    open fun prefixFactory(matcher: Rule<Statement>, prefixExpression: (Expression) -> Expression): Rule<Statement> =
        sequence(
            matcher,
            zeroOrMore(whitespace()),
            primaryExpression(),
            push {
                val expression = pop(it) as Expression
                prefixExpression(expression)
            }
        )

    open fun binaryExpression(): Rule<Statement> = choice(
        arithmetic(),
        logicExpression(),
    )

    open fun logicExpression(): Rule<Statement> = choice(
        binaryFactory(string("==")) { left, right -> LogicExpression.Equals(left, right) },
        binaryFactory(string("!=")) { left, right -> PrefixExpression.Not(LogicExpression.Equals(left, right)) },
        binaryFactory(string("===")) { left, right -> LogicExpression.NonCoercingEquals(left, right) },
        binaryFactory(string("<")) { left, right -> LogicExpression.LessThan(left, right) },
        binaryFactory(string("<=")) { left, right -> LogicExpression.LessThanOrEqual(left, right) },
        binaryFactory(string(">")) { left, right -> LogicExpression.GreaterThan(left, right) },
        binaryFactory(string(">=")) { left, right -> LogicExpression.GreaterThanOrEqual(left, right) },
        binaryFactory(string("!==")) { left, right ->
            PrefixExpression.Not(
                LogicExpression.NonCoercingEquals(
                    left,
                    right
                )
            )
        },
        binaryFactory(string("||")) { left, right -> LogicExpression.LogicalOr(left, right) },
        binaryFactory(string("&&")) { left, right -> LogicExpression.LogicalAnd(left, right) },
    )

    open fun arithmetic(): Rule<Statement> = choice(
        binaryFactory(plus) { left, right -> ArithmeticExpression.Addition(left, right) },
        binaryFactory(minus) { left, right -> ArithmeticExpression.Subtraction(left, right) },
    )

    open fun precedenceArithmetic(): Rule<Statement> = choice(
        binaryFactory(star) { left, right -> ArithmeticExpression.Multiplication(left, right) },
        binaryFactory(slash) { left, right -> ArithmeticExpression.Division(left, right) },
        binaryFactory(percent) { left, right -> ArithmeticExpression.Modulo(left, right) },
    )

    open fun binaryFactory(
        matcher: Rule<Statement>,
        binaryExpression: (Expression, Expression) -> Statement,
    ): Rule<Statement> = sequence(
        zeroOrMore(sameLineWhitespace()),
        matcher,
        zeroOrMore(whitespace()),
        primaryExpression(),
        zeroOrMore(sameLineWhitespace()),
        push {
            val right = pop(it) as Expression
            val left = pop(it) as Expression
            binaryExpression(left, right)
        },
    )

    open fun continuation(): Rule<Statement> = sequence(
        zeroOrMore(sameLineWhitespace()),
        choice(
            asFunctionCall(),
            asArrayReference(),
            asChainedCall(),
        ),
    )

    open fun asChainedCall(): Rule<Statement> = sequence(
        period,
        zeroOrMore(whitespace()),
        primaryExpression(),
        zeroOrMore(sameLineWhitespace()),
        push {
            val right = pop(it) as Expression
            val left = pop(it) as Expression
            ChainedCall(left, right)
        },
    )

    open fun asArrayReference(): Rule<Statement> = sequence(
        leftSquareBracket,
        zeroOrMore(whitespace()),
        expression(),
        zeroOrMore(whitespace()),
        rightSquareBracket,
        push {
            val reference = pop(it) as Expression
            val array = pop(it) as Expression
            ArrayReference(array, reference)
        },
    )

    open fun asFunctionCall(): Rule<Statement> = sequence(
        leftParenthesis,
        zeroOrMore(whitespace()),
        push {
            val expression = pop(it) as Expression
            FunctionCall(expression)
        },
        optional(
            sequence(
                expression(),
                zeroOrMore(sameLineWhitespace()),
                push {
                    val parameter = pop(it) as Expression
                    val functionCall = pop(it) as FunctionCall
                    functionCall.withParameters(parameter)
                },
                zeroOrMore(
                    sequence(
                        comma,
                        zeroOrMore(whitespace()),
                        expression(),
                        zeroOrMore(sameLineWhitespace()),
                        push {
                            val parameter = pop(it) as Expression
                            val functionCall = pop(it) as FunctionCall
                            functionCall.withParameters(parameter)
                        },
                    ),
                ),
            ),
        ),
        zeroOrMore(whitespace()),
        rightParenthesis,
    )

    open fun syntaticLiteral(): Rule<Statement> = choice(
        objectLiteral(),
        array(),
    )

    open fun objectLiteral() = sequence(
        leftCurlyBracket,
        push { ObjectLiteral() },
        optional(
            sequence(
                objectProperty(),
                zeroOrMore(
                    sequence(
                        zeroOrMore(whitespace()),
                        comma,
                        objectProperty(),
                    ),
                ),
                zeroOrMore(whitespace()),
                optional(
                    sequence(
                        comma,
                        zeroOrMore(whitespace()),
                    ),
                ),
            )
        ),
        rightCurlyBracket,
    )

    open fun objectProperty() = sequence(
        zeroOrMore(whitespace()),
        identifierGrammar.start(),
        zeroOrMore(whitespace()),
        colon,
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
        leftSquareBracket,
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
                        comma,
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
                        comma,
                    ),
                ),
            ),
        ),

        zeroOrMore(whitespace()),
        rightSquareBracket,
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

    open fun lvalueExpressions(): Rule<Statement> = sequence(
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

    open fun lvalueExpression(): Rule<Statement> = sequence(
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

    open fun ternaryExpression(): Rule<Statement> = sequence(
        zeroOrMore(sameLineWhitespace()),
        char('?'),
        zeroOrMore(whitespace()),
        primaryExpression(),
        zeroOrMore(whitespace()),
        char(':'),
        zeroOrMore(whitespace()),
        primaryExpression(),

        push {
            val expression = pop(it) as Expression
            val right = pop(it) as Expression
            val left = pop(it) as Expression
            TernaryExpression(left, right, expression)
        }
    )

    override fun whitespace(): Rule<Statement> = choice(
        super.whitespace(),
        comment(),
    )

    open fun ignorable() = choice(
        semiColon,
        whitespace(),
    )

    open fun comment(): Rule<Statement> = blockComment() or lineComment()

    open fun lineComment(): Rule<Statement> =
        string("//") + zeroOrMore(noneOfChars("\n\r"))

    open fun blockComment(): Rule<Statement> =
        string("/*") + zeroOrMore(testNot(string("*/")) + anyChar()) + string("*/")

    open fun sameLineWhitespace(): Rule<Statement> = sequence(
        testNot(cr()),
        testNot(lf()),
        whitespace(),
    )

    open fun codeSeparator() = choice(
        semiColon,
        lf(),
        cr(),
        crlf(),
    )

    val colon = char(':')
    val semiColon = char(';')
    val period = char('.')
    val comma = char(',')

    val leftParenthesis = char('(')
    val rightParenthesis = char(')')
    val leftSquareBracket = char('[')
    val rightSquareBracket = char(']')
    val leftCurlyBracket = char('{')
    val rightCurlyBracket = char('}')

    val equalChar = char('=')
    val plus = char('+')
    val exclamation = char('!')
    val minus = char('-')
    val star = char('*')
    val slash = char('/')
    val percent = char('%')
}