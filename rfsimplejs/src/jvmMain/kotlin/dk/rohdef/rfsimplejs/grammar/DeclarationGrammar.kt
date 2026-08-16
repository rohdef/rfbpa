package dk.rohdef.rfsimplejs.grammar

import com.github.mpe85.grampa.grammar.AbstractGrammar
import dk.rohdef.rfsimplejs.ast2.Declaration
import dk.rohdef.rfsimplejs.ast2.IdentifierGrammar
import dk.rohdef.rfsimplejs.ast2.JavaScriptGrammar
import dk.rohdef.rfsimplejs.ast2.statement.Expression
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.Statement

open class DeclarationGrammar(
     private val javaScriptGrammar: JavaScriptGrammar = JavaScriptGrammar()
) : AbstractGrammar<Statement>() {
    private val identifierGrammar = IdentifierGrammar()

    override fun start() = declaration()

    open fun declaration() = choice(
        variable(),
        constant(),
        let(),
    )

    open fun variable() = sequence(
        sequence(
            string("var"),
            lvalueExpression(),
        ),
        push {
            val expression = pop(it) as Expression
            val lvalue = pop(it) as Identifier
            Declaration.Variable(lvalue, expression)
        }
    )

    open fun constant() = sequence(
        sequence(
            string("const"),
            lvalueExpression(),
        ),
        push {
            val expression = pop(it) as Expression
            val lvalue = pop(it) as Identifier
            Declaration.Constant(lvalue, expression)
        }
    )

    open fun let() = sequence(
        sequence(
            string("let"),
            lvalueExpression(),
        ),
        push {
            val expression = pop(it) as Expression
            val lvalue = pop(it) as Identifier
            Declaration.Let(lvalue, expression)
        }
    )

    open fun lvalueExpression() = sequence(
        oneOrMore(whitespace()),
        identifierGrammar.identifier(),
        zeroOrMore(sameLineWhitespace()),
        char('='),
        zeroOrMore(whitespace()),
        javaScriptGrammar.expression(),
    )

    open fun sameLineWhitespace() = sequence(
        testNot(cr()),
        testNot(lf()),
        whitespace(),
    )
}