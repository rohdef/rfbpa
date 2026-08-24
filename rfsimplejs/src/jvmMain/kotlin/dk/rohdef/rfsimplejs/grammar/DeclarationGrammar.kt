package dk.rohdef.rfsimplejs.grammar

import com.github.mpe85.grampa.grammar.AbstractGrammar
import dk.rohdef.rfsimplejs.ast2.Declaration
import dk.rohdef.rfsimplejs.ast2.IdentifierGrammar
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.Statement

open class DeclarationGrammar(
    private val javaScriptGrammar: JavaScriptGrammar = JavaScriptGrammar(),
    private val identifierGrammar: IdentifierGrammar = IdentifierGrammar(),
) : AbstractGrammar<Statement>() {
    override fun start() = declaration()

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
            identifierGrammar.identifier(),
            zeroOrMore(sameLineWhitespace()),
            char('='),
            zeroOrMore(whitespace()),
            javaScriptGrammar.expression(),
            zeroOrMore(sameLineWhitespace()),
        ),
        push {
            val expression = pop(it) as Expression
            val lvalue = pop(it) as Identifier
            val declaration = pop(it) as Declaration<*>
            declaration.withDeclarion(lvalue, expression)
        },
    )

    open fun sameLineWhitespace() = sequence(
        testNot(cr()),
        testNot(lf()),
        whitespace(),
    )
}