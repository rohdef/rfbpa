package dk.rohdef.rfsimplejs

import com.github.mpe85.grampa.parser.Parser
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.core.spec.style.scopes.FunSpecRootScope
import io.kotest.datatest.withContexts

fun FunSpecRootScope.parserContexts(
    first: ParserSpec,
    second: ParserSpec,
    vararg rest: ParserSpec,
    test: suspend FunSpecContainerScope.(
        Parser<Statement>,
        (Statement) -> Statement,
    ) -> Unit
) {
    this.withContexts(
        nameFn = { it.name },
        first,
        second,
        *rest,
    ) { spec ->
        test(spec.parser, spec.wrapping)
    }
}