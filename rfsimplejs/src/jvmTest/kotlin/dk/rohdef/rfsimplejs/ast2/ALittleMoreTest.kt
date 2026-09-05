package dk.rohdef.rfsimplejs.ast2

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.Identifier
import dk.rohdef.rfsimplejs.ast2.statement.literals.ArrayLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.ObjectLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.StringLiteral
import dk.rohdef.rfsimplejs.ast2.statement.literals.Boolean as AstBoolean
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

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

    test("let's stretch") {
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
})