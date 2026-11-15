package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.*
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.ArithmeticExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.BitwiseExpression
import dk.rohdef.rfsimplejs.ast2.statement.binaryExpressions.LogicExpression
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Number
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.logic.ForStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.IfStatement
import dk.rohdef.rfsimplejs.ast2.statement.logic.Switch
import dk.rohdef.rfsimplejs.ast2.statement.logic.SwitchCase
import dk.rohdef.rfsimplejs.ast2.statement.logic.TernaryExpression
import dk.rohdef.rfsimplejs.ast2.statement.logic.WhileStatement
import dk.rohdef.rfsimplejs.grammar.JavaScriptGrammar
import io.kotest.core.annotation.DisplayName
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean

@DisplayName("Combinations of code")
class ALittleMoreTest : FunSpec({
    val grammar = JavaScriptGrammar::class.createGrammar()
    val parser = Parser(grammar)

    test("semicolon") {
        val code = """
            "use strict";
        """.trimIndent()

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            StringLiteral("use strict"),
        )
    }

    test("using various variables") {
        val code = """
            "use strict";
            var TOK="id-10-t", ERFV=false;

            var UGEDAGE=['søndag','mandag','tirsdag','onsdag','torsdag','fredag','lørdag'];
            var UGEKORT=['søn','man','tir','ons','tor','fre','lør'];
            var MDR=['januar','februar','marts','april','maj','juni','juli','august','september','oktober','november','december'];
            var MDRK=['jan','feb','mar','apr','maj','jun','jul','aug','sep','okt','nov','dec'];
            var GRUPPENAVN={dag:'Dagvagt',aften:'Aftenvagt',nat:'Nattevagt',lang:'Lang vagt',doegn:'Døgnvagt',syge:'Sygevagt'};
        """.trimIndent()

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            StringLiteral("use strict"),

            Declaration.Variable(
                Identifier("TOK") to StringLiteral("id-10-t"),
                Identifier("ERFV") to AstBoolean.False,
            ),

            Declaration.Variable(
                Identifier("UGEDAGE") to ArrayLiteral(
                    StringLiteral("søndag"),
                    StringLiteral("mandag"),
                    StringLiteral("tirsdag"),
                    StringLiteral("onsdag"),
                    StringLiteral("torsdag"),
                    StringLiteral("fredag"),
                    StringLiteral("lørdag"),
                ),
            ),

            Declaration.Variable(
                Identifier("UGEKORT") to ArrayLiteral(
                    StringLiteral("søn"),
                    StringLiteral("man"),
                    StringLiteral("tir"),
                    StringLiteral("ons"),
                    StringLiteral("tor"),
                    StringLiteral("fre"),
                    StringLiteral("lør"),
                ),
            ),

            Declaration.Variable(
                Identifier("MDR") to ArrayLiteral(
                    StringLiteral("januar"),
                    StringLiteral("februar"),
                    StringLiteral("marts"),
                    StringLiteral("april"),
                    StringLiteral("maj"),
                    StringLiteral("juni"),
                    StringLiteral("juli"),
                    StringLiteral("august"),
                    StringLiteral("september"),
                    StringLiteral("oktober"),
                    StringLiteral("november"),
                    StringLiteral("december"),
                ),
            ),

            Declaration.Variable(
                Identifier("MDRK") to ArrayLiteral(
                    StringLiteral("jan"),
                    StringLiteral("feb"),
                    StringLiteral("mar"),
                    StringLiteral("apr"),
                    StringLiteral("maj"),
                    StringLiteral("jun"),
                    StringLiteral("jul"),
                    StringLiteral("aug"),
                    StringLiteral("sep"),
                    StringLiteral("okt"),
                    StringLiteral("nov"),
                    StringLiteral("dec"),
                ),
            ),

            Declaration.Variable(
                Identifier("GRUPPENAVN") to ObjectLiteral(
                    Identifier("dag") to StringLiteral("Dagvagt"),
                    Identifier("aften") to StringLiteral("Aftenvagt"),
                    Identifier("nat") to StringLiteral("Nattevagt"),
                    Identifier("lang") to StringLiteral("Lang vagt"),
                    Identifier("doegn") to StringLiteral("Døgnvagt"),
                    Identifier("syge") to StringLiteral("Sygevagt"),
                ),
            )
        )
    }

    test("defining functions") {
        val code = """
            function iso(d){var m=d.getMonth()+1,g=d.getDate();return d.getFullYear()+'-'+(m<10?'0':'')+m+'-'+(g<10?'0':'')+g;}
            function fraIso(s){var d=String(s).split('-');return new Date(+d[0],+d[1]-1,+d[2]);}
        """.trimIndent()

        val result = parser.run(code)
        val program = result.stackTop

        program shouldBe Program(
            Declaration.Variable(
                Identifier("iso") to
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(Identifier("d")),
                            BlockStatement(
                                Declaration.Variable(
                                    Identifier("m") to
                                            ArithmeticExpression.Addition(
                                                ChainedCall(
                                                    Identifier("d"),
                                                    FunctionCall(
                                                        Identifier("getMonth")
                                                    ),
                                                ),
                                                Number.SimpleNumber.IntegerNumber(1),
                                            ),
                                    Identifier("g") to
                                            ChainedCall(
                                                Identifier("d"),
                                                FunctionCall(
                                                    Identifier("getDate")
                                                ),
                                            )
                                ),

                                Return.Expression(
                                    ArithmeticExpression.Addition(
                                        ArithmeticExpression.Addition(
                                            ArithmeticExpression.Addition(
                                                ArithmeticExpression.Addition(
                                                    ArithmeticExpression.Addition(
                                                        ArithmeticExpression.Addition(
                                                            ChainedCall(
                                                                Identifier("d"),
                                                                FunctionCall(
                                                                    Identifier("getFullYear")
                                                                ),
                                                            ),
                                                            StringLiteral("-"),
                                                        ),
                                                        EnclosedExpression(
                                                            TernaryExpression(
                                                                LogicExpression.LessThan(
                                                                    Identifier("m"),
                                                                    Number.SimpleNumber.IntegerNumber(10)
                                                                ),
                                                                StringLiteral("0"),
                                                                StringLiteral(""),
                                                            ),
                                                        ),
                                                    ),
                                                    Identifier("m"),
                                                ),
                                                StringLiteral("-"),
                                            ),
                                            EnclosedExpression(
                                                TernaryExpression(
                                                    LogicExpression.LessThan(
                                                        Identifier("g"),
                                                        Number.SimpleNumber.IntegerNumber(10)
                                                    ),
                                                    StringLiteral("0"),
                                                    StringLiteral(""),
                                                ),
                                            ),
                                        ),
                                        Identifier("g"),
                                    ),
                                ),
                            ),
                        ),
            ),

            Declaration.Variable(
                Identifier("fraIso") to
                        FunctionExpression(
                            FunctionExpression.Parameters()
                                .withParameter(Identifier("s")),
                            BlockStatement(
                                Declaration.Variable(
                                    Identifier("d") to
                                            ChainedCall(
                                                FunctionCall(
                                                    Identifier("String"),
                                                    Identifier("s"),
                                                ),
                                                FunctionCall(
                                                    Identifier("split"),
                                                    StringLiteral("-"),
                                                ),
                                            ),
                                ),
                                Return.Expression(
                                    PrefixExpression.New(
                                        FunctionCall(
                                            Identifier("Date"),
                                            PrefixExpression.Plus(
                                                ArrayReference(
                                                    Identifier("d"),
                                                    Number.SimpleNumber.IntegerNumber(0)
                                                ),
                                            ),
                                            ArithmeticExpression.Subtraction(
                                                PrefixExpression.Plus(
                                                    ArrayReference(
                                                        Identifier("d"),
                                                        Number.SimpleNumber.IntegerNumber(1)
                                                    ),
                                                ),
                                                Number.SimpleNumber.IntegerNumber(1),
                                            ),
                                            PrefixExpression.Plus(
                                                ArrayReference(
                                                    Identifier("d"),
                                                    Number.SimpleNumber.IntegerNumber(2),
                                                )
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),
            ),
        )
    }

    test("funny") {
        val code = """
                function tidSegmenter(t){
                  var l=Math.max(0,Math.min(1440,+(t&&t.l)||0));
                  if(l>=1440) return [[0,1440]];
                  var s=((((+(t&&t.s)||0))%1440)+1440)%1440;
                  // The return is causing parse problems... why?
                  return (s+l<=1440) ? [[s,s+l]] : [[s,1440],[0,s+l-1440]];
                }
            """.trimIndent()

        val result = parser.run(code)
            .stackTop

        result shouldBe Program(
            Declaration.Variable(
                Identifier("tidSegmenter") to FunctionExpression(
                    FunctionExpression.Parameters()
                        .withParameter("t"),
                    BlockStatement(
                        Declaration.Variable(
                            Identifier("l") to ChainedCall(
                                Identifier("Math"),
                                FunctionCall(
                                    Identifier("max"),
                                    Number.SimpleNumber.IntegerNumber(0),
                                    ChainedCall(
                                        Identifier("Math"),
                                        FunctionCall(
                                            Identifier("min"),
                                            Number.SimpleNumber.IntegerNumber(1440),
                                            LogicExpression.LogicalOr(
                                                PrefixExpression.Plus(
                                                    EnclosedExpression(
                                                        LogicExpression.LogicalAnd(
                                                            Identifier("t"),
                                                            ChainedCall(
                                                                Identifier("t"),
                                                                Identifier("l"),
                                                            ),
                                                        )
                                                    ),
                                                ),
                                                Number.SimpleNumber.IntegerNumber(0),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                        ),

                        IfStatement(
                            LogicExpression.GreaterThanOrEqual(
                                Identifier("l"),
                                Number.SimpleNumber.IntegerNumber(1440),
                            ),
                            Return.Expression(
                                ArrayLiteral(
                                    ArrayLiteral(
                                        Number.SimpleNumber.IntegerNumber(0),
                                        Number.SimpleNumber.IntegerNumber(1440),
                                    ),
                                ),
                            ),
                        ),

                        Declaration.Variable(
                            Identifier("s") to ArithmeticExpression.Modulo(
                                EnclosedExpression(
                                    ArithmeticExpression.Addition(
                                        EnclosedExpression(
                                            ArithmeticExpression.Modulo(
                                                EnclosedExpression(
                                                    EnclosedExpression(
                                                        LogicExpression.LogicalOr(
                                                            PrefixExpression.Plus(
                                                                EnclosedExpression(
                                                                    LogicExpression.LogicalAnd(
                                                                        Identifier("t"),
                                                                        ChainedCall(
                                                                            Identifier("t"),
                                                                            Identifier("s"),
                                                                        ),
                                                                    ),
                                                                ),
                                                            ),
                                                            Number.SimpleNumber.IntegerNumber(0),
                                                        ),
                                                    ),
                                                ),
                                                Number.SimpleNumber.IntegerNumber(1440),
                                            ),
                                        ),
                                        Number.SimpleNumber.IntegerNumber(1440)
                                    ),
                                ),
                                Number.SimpleNumber.IntegerNumber(1440),
                            ),
                        ),

                        Return.Expression(
                            TernaryExpression(
                                EnclosedExpression(
                                    LogicExpression.LessThanOrEqual(
                                        ArithmeticExpression.Addition(
                                            Identifier("s"),
                                            Identifier("l")
                                        ),
                                        Number.SimpleNumber.IntegerNumber(1440),
                                    ),
                                ),
                                ArrayLiteral(
                                    ArrayLiteral(
                                        Identifier("s"),
                                        ArithmeticExpression.Addition(
                                            Identifier("s"),
                                            Identifier("l"),
                                        ),
                                    ),
                                ),
                                ArrayLiteral(
                                    ArrayLiteral(
                                        Identifier("s"),
                                        Number.SimpleNumber.IntegerNumber(1440),
                                    ),
                                    ArrayLiteral(
                                        Number.SimpleNumber.IntegerNumber(0),
                                        ArithmeticExpression.Subtraction(
                                            ArithmeticExpression.Addition(
                                                Identifier("s"),
                                                Identifier("l"),
                                            ),
                                            Number.SimpleNumber.IntegerNumber(1440),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    context("statements without semicolon or newline separators") {
        test("for loop followed by function call") {
            val code = "for(var i=0;i<navn.length;i++){i} foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForStatement(
                    Declaration.Variable(
                        Identifier("i") to Number.SimpleNumber.IntegerNumber(0),
                    ),
                    LogicExpression.LessThan(
                        Identifier("i"),
                        ChainedCall(
                            Identifier("navn"),
                            Identifier("length"),
                        ),
                    ),
                    PostfixExpression.Increment(
                        Identifier("i"),
                    ),
                    BlockStatement(
                        Identifier("i"),
                    ),
                ),
                FunctionCall(
                    Identifier("foo"),
                ),
            )
        }

        test("for loop followed by variable declaration") {
            val code = "for (var i = 0; i < 10; i++) { x; } var y = 1;"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForStatement(
                    Declaration.Variable(
                        Identifier("i") to Number.SimpleNumber.IntegerNumber(0),
                    ),
                    LogicExpression.LessThan(
                        Identifier("i"),
                        Number.SimpleNumber.IntegerNumber(10),
                    ),
                    PostfixExpression.Increment(
                        Identifier("i"),
                    ),
                    BlockStatement(
                        Identifier("x"),
                    ),
                ),
                Declaration.Variable(
                    Identifier("y") to Number.SimpleNumber.IntegerNumber(1),
                ),
            )
        }

        test("if statement followed by function call") {
            val code = "if (a) { b(); } foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Identifier("a"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("b"),
                        ),
                    ),
                ),
                FunctionCall(
                    Identifier("foo"),
                ),
            )
        }

        test("if else statement followed by function call") {
            val code = "if (a) { b(); } else { c(); } foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                IfStatement(
                    Identifier("a"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("b"),
                        ),
                    ),
                    BlockStatement(
                        FunctionCall(
                            Identifier("c"),
                        ),
                    ),
                ),
                FunctionCall(
                    Identifier("foo"),
                ),
            )
        }

        test("while loop followed by function call") {
            val code = "while (a) { b(); } foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                WhileStatement(
                    Identifier("a"),
                    BlockStatement(
                        FunctionCall(
                            Identifier("b"),
                        ),
                    ),
                ),
                FunctionCall(
                    Identifier("foo"),
                ),
            )
        }

        test("function declaration followed by function call") {
            val code = "function bar() { return 1; } foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Declaration.Variable(
                    Identifier("bar") to FunctionExpression(
                        FunctionExpression.Parameters(),
                        BlockStatement(
                            Return.Expression(
                                Number.SimpleNumber.IntegerNumber(1),
                            ),
                        ),
                    ),
                ),
                FunctionCall(
                    Identifier("foo"),
                ),
            )
        }

        test("switch statement followed by function call") {
            val code = "switch (x) { case 1: break; } foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                Switch(
                    Identifier("x"),
                    listOf(
                        SwitchCase.Match(
                            Number.SimpleNumber.IntegerNumber(1),
                            BlockStatement(
                                Break,
                            ),
                        ),
                    ),
                ),
                FunctionCall(
                    Identifier("foo"),
                ),
            )
        }

        test("consecutive block statements without separators") {
            val code = "for (var i = 0; i < 1; i++) { a; } if (b) { c; } foo();"

            val result = parser.run(code)
            val program = result.stackTop

            program shouldBe Program(
                ForStatement(
                    Declaration.Variable(
                        Identifier("i") to Number.SimpleNumber.IntegerNumber(0),
                    ),
                    LogicExpression.LessThan(
                        Identifier("i"),
                        Number.SimpleNumber.IntegerNumber(1),
                    ),
                    PostfixExpression.Increment(
                        Identifier("i"),
                    ),
                    BlockStatement(
                        Identifier("a"),
                    ),
                ),
                IfStatement(
                    Identifier("b"),
                    BlockStatement(
                        Identifier("c"),
                    ),
                ),
                FunctionCall(
                    Identifier("foo"),
                ),
            )
        }
    }
})