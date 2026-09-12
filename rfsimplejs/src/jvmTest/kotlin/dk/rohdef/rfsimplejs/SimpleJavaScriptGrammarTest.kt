//class SimpleJavaScriptGrammarTest {
//    private val grammar = SimpleJavaScript::class.createGrammar()
//    private val parser = Parser(grammar)
//
//    @Nested
//    inner class `Name expression` {
//        @Test
//        fun `whitespace wrapped text`() {
//            val text = "  \t  tardigrade \n  "
//
//            val result = parser.run(text)
//                .stackTop
//
//            result shouldBe Name("tardigrade")
//        }
//    }
//
//    @Nested
//    inner class `Multiple sequential expressions` {
//        @Test
//        fun `parse simple expressions`() {
//            val text = "foo(a); bar(b)"
//
//            val result = parser.run(text)
//                .stackTop
//
//            result shouldBe Expressions(
//                listOf(
//                    FunctionCall(
//                        Name("foo"),
//                        Name("a")
//                    ),
//                    FunctionCall(
//                        Name("bar"),
//                        Name("b")
//                    ),
//                )
//            )
//        }
//    }
//
//
//    @Test
//    fun `parse hf thing`() {
//        val text =
//            "windowopen('/citizen_web/index.php?&module_type=AXP&modInstId=20&getting_popped=1&axp_silent_run=1&quiet_run=1&getting_popped=1&act=shift_plan&sub_act=editbooking&date=1782079200&shifttype=4&booking=3239027&displaytype=1&axp_startdate=1782079200'); cancelBubbling(event);\n"
//
//        val result = parser.run(text)
//            .stackTop
//
//        result shouldBe Expressions(
//            listOf(
//                FunctionCall(
//                    Name("windowopen"),
//                    Text("/citizen_web/index.php?&module_type=AXP&modInstId=20&getting_popped=1&axp_silent_run=1&quiet_run=1&getting_popped=1&act=shift_plan&sub_act=editbooking&date=1782079200&shifttype=4&booking=3239027&displaytype=1&axp_startdate=1782079200"),
//                ),
//                FunctionCall(
//                    Name("cancelBubbling"),
//                    Name("event"),
//                )
//            )
//        )
//    }
//}
