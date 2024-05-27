package dk.rohdef.rfsimplejs

import com.github.mpe85.grampa.createGrammar
import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast.*
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import kotlin.test.Ignore
import kotlin.test.Test

class SimpleJavaScriptGrammarTest {
    private val grammar = SimpleJavaScript::class.createGrammar()
    private val parser = Parser(grammar)

    @Nested
    inner class `Not grammar` {
        @Test
        fun `empty content`() {
            val text = ""

            val result = parser.run(text)
                .stackTop

            result shouldBe null
        }

        @Test
        fun `whitespaces`() {
            val text = "  \t  "

            val result = parser.run(text)
                .stackTop

            result shouldBe null
        }
    }

    @Nested
    inner class `Name expression` {
        @Test
        fun `simple name`() {
            val text = "heya"

            val result = parser.run(text)
                .stackTop

            result shouldBe Name("heya")
        }

        @Test
        fun `name with numbers`() {
            val text = "l33t"

            val result = parser.run(text)
                .stackTop

            result shouldBe Name("l33t")
        }

        @Test
        fun `whitespace wrapped text`() {
            val text = "  \t  tardigrade \n  "

            val result = parser.run(text)
                .stackTop

            result shouldBe Name("tardigrade")
        }

        @Test
        fun `name can be $`() {
            val text = "$"

            val result = parser.run(text)
                .stackTop

            result shouldBe Name("$")
        }

        // TODO currently very simple rules,
        //  dash and underscore needs added for completion
    }

    @Nested
    inner class `Quote text` {
        @Test
        fun `empty`() {
            val text = "''"

            val result = parser.run(text)
                .stackTop

            result shouldBe Text("")
        }

        @Test
        fun `random chars`() {
            val text = "'hællø_42@#$...com  '"

            val result = parser.run(text)
                .stackTop

            result shouldBe Text("hællø_42@#\$...com  ")
        }

        // TODO at minimum escapes needs to be handled
        // TODO if I recall right new line is not legit - it has to be escape, test!
    }

    @Nested
    inner class `Function call` {
        // TODO
    }

    @Nested
    inner class `Chained function calls` {
        @Test
        fun `string`() {
            val text = "'foo'.toUpper(true)"

            val result = parser.run(text)
                .stackTop

            result shouldBe ChainedCall(
                Text("foo"),
                FunctionCall(
                    Name("toUpper"),
                    Name("true"),
                ),
            )
        }

        @Test
        fun `function`() {
            val text = "bar('42').send('somewhere')"

            val result = parser.run(text)
                .stackTop

            result shouldBe ChainedCall(
                FunctionCall(
                    Name("bar"),
                    Text("42"),
                ),
                FunctionCall(
                    Name("send"),
                    Text("somewhere"),
                ),
            )
        }

        @Test
        fun `name`() {
            val text = "baz.send('somewhere')"

            val result = parser.run(text)
                .stackTop

            result shouldBe ChainedCall(
                Name("baz"),
                FunctionCall(
                    Name("send"),
                    Text("somewhere"),
                ),
            )
        }

        @Test
        @Ignore
        fun `Multiple chains`() {
            // TODO improve parser
            val text = "window.url(false).noWay('somewhere')"

            val result = parser.run(text)
                .stackTop

            result shouldBe ChainedCall(
                ChainedCall(
                    Name("window"),
                    FunctionCall(
                        Name("url"),
                        Name("false"),
                    ),
                ),
                FunctionCall(
                    Name("noWay"),
                    Text("somewhere"),
                ),
            )
        }
    }

    @Nested
    inner class `Multiple sequential expressions` {
        @Test
        fun `parse simple expressions`() {
            val text = "foo(a); bar(b)"

            val result = parser.run(text)
                .stackTop

            result shouldBe Expressions(
                listOf(
                    FunctionCall(
                        Name("foo"),
                        Name("a")
                    ),
                    FunctionCall(
                        Name("bar"),
                        Name("b")
                    ),
                )
            )
        }
    }

    @Test
    fun `parse hf thing`() {
        val text =
            "windowopen('/citizen_web/index.php?&module_type=AXP&modInstId=20&axp_silent_run=1&quiet_run=1&getting_popped=1&act=shift_plan&sub_act=editbooking&date=1684706400&shifttype=1&booking=4004211&displaytype=1&axp_startdate=1684706400'); cancelBubbling(event);\n"

        val result = parser.run(text)
            .stackTop

        result shouldBe Expressions(
            listOf(
                FunctionCall(
                    Name("windowopen"),
                    Text("/citizen_web/index.php?&module_type=AXP&modInstId=20&axp_silent_run=1&quiet_run=1&getting_popped=1&act=shift_plan&sub_act=editbooking&date=1684706400&shifttype=1&booking=4004211&displaytype=1&axp_startdate=1684706400"),
                ),
                FunctionCall(
                    Name("cancelBubbling"),
                    Name("event"),
                )
            )
        )
    }

    @Test
    fun `parse other hf thing`() {
        val text =
            "toolTip('Vagt type: Dag<br>Fra: 22-05-2023 07:15<br>Til: 22-05-2023 19:00<br>Handicaphjælper<br>Vikarnummer: 91423<br>Fiktivus Maximus<br>Telefon: +4511223344<br>Mobil: 55667788<br>Ordrenummer: 133771'); $('.booking_4004211').addClass('booking_over');\n"

        val result = parser.run(text)
            .stackTop

        val expressions = result.shouldBeInstanceOf<Expressions>()
        expressions.expressions.shouldHaveSize(2)
        val toolTip = expressions.expressions.get(0)
        val jQuery = expressions.expressions.get(1)

        toolTip shouldBe FunctionCall(
            Name("toolTip"),
            Text("Vagt type: Dag<br>Fra: 22-05-2023 07:15<br>Til: 22-05-2023 19:00<br>Handicaphjælper<br>Vikarnummer: 91423<br>Fiktivus Maximus<br>Telefon: +4511223344<br>Mobil: 55667788<br>Ordrenummer: 133771"),
        )
        jQuery shouldBe ChainedCall(
            FunctionCall(
                Name("$"),
                Text(".booking_4004211"),
            ),
            FunctionCall(
                Name("addClass"),
                Text("booking_over"),
            ),
        )
    }
}
