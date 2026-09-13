package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
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

    open fun expression(): Rule<Statement> = choice(
        binaryExpression(),
        primaryExpression(),
    )

    open fun prefixExpression(): Rule<Statement> = choice(
        prefixFactory(string("typeof ")) { expression -> PrefixExpression.TypeOf(expression) },
        prefixFactory(string("delete ")) { expression -> PrefixExpression.Delete(expression) },
        prefixFactory(string("new ")) { expression -> PrefixExpression.New(expression) },
        prefixFactory(string("void ")) { expression -> PrefixExpression.Void(expression) },
        prefixFactory(string("await ")) { expression -> PrefixExpression.Await(expression) },
        prefixFactory(string("++")) { expression -> PrefixExpression.Increment(expression) },
        prefixFactory(string("--")) { expression -> PrefixExpression.Decrement(expression) },
        prefixFactory(exclamation) { expression -> PrefixExpression.Not(expression) },
        prefixFactory(plus) { expression -> PrefixExpression.Plus(expression) },
        prefixFactory(minus) { expression -> PrefixExpression.Minus(expression) },
    )

    open fun prefixFactory(matcher: Rule<Statement>, prefixExpression: (Expression) -> Expression): Rule<Statement> = sequence(
        matcher,
        zeroOrMore(whitespace()),
        expression(),
        push {
            val expression = pop(it) as Expression
            prefixExpression(expression)
        }
    )

    open fun expressionWithoutBinary(): Rule<Statement> = primaryExpression()

    open fun binaryExpression(): Rule<Statement> = choice(
        arithmetic(),
        logicExpression(),
    )

    open fun logicExpression(): Rule<Statement> = choice(
        binaryFactory(string("==")) { left, right -> LogicExpression.Equals(left, right) },
        binaryFactory(string("!=")) { left, right -> PrefixExpression.Not(LogicExpression.Equals(left, right)) },
        binaryFactory(string("===")) { left, right -> LogicExpression.NonCoercingEquals(left, right) },
        binaryFactory(string("!==")) { left, right -> PrefixExpression.Not(LogicExpression.NonCoercingEquals(left, right)) },
        binaryFactory(string("||")) { left, right -> LogicExpression.LogicalOr(left, right) },
        binaryFactory(string("&&")) { left, right -> LogicExpression.LogicalAnd(left, right) },
    )

    open fun arithmetic(): Rule<Statement> = choice(
        binaryFactory(plus) { left, right -> ArithmeticExpression.Addition(left, right) },
        binaryFactory(minus) { left, right -> ArithmeticExpression.Subtraction(left, right) },
        binaryFactory(star) { left, right -> ArithmeticExpression.Multiplication(left, right) },
        binaryFactory(slash) { left, right -> ArithmeticExpression.Division(left, right) },
        binaryFactory(percent) { left, right -> ArithmeticExpression.Modulo(left, right) },
    )

    open fun binaryFactory(
        matcher: Rule<Statement>,
        binaryExpression: (Expression, Expression) -> Statement,
    ): Rule<Statement> = sequence(
        expressionWithoutBinary(),
        zeroOrMore(sameLineWhitespace()),
        matcher,
        zeroOrMore(whitespace()),
        expression(),
        zeroOrMore(sameLineWhitespace()),
        push {
            val right = pop(it) as Expression
            val left = pop(it) as Expression
            binaryExpression(left, right)
        },
    )

    open fun primaryExpression(): Rule<Statement> = sequence(
        choice(
            prefixExpression(),
            literalGrammar.start(),
            identifierGrammar.start(),
            syntaticLiteral(),
        ),
        optional(continuation()),
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
        expression(),
        zeroOrMore(sameLineWhitespace()),
        push {
            val right = pop(it) as Expression
            val left = pop(it) as Expression
            ChainedCall(left, right)
        },
        optional(continuation()),
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
        optional(continuation()),
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
        rightParenthesis,
        optional(continuation()),
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
        semiColon,
        whitespace(),
    )

    open fun sameLineWhitespace() = sequence(
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

    val exclamation = char('!')
    val plus = char('+')
    val minus = char('-')
    val star = char('*')
    val slash = char('/')
    val percent = char('%')
    val pipe = char('|')
    val equalChar = char('=')
}