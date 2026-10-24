package dk.rohdef.rfsimplejs

import com.github.mpe85.grampa.event.MatchSuccessEvent
import com.github.mpe85.grampa.event.ParseEventListener
import com.github.mpe85.grampa.event.PreParseEvent
import dk.rohdef.rfsimplejs.ast2.statement.Statement
import io.github.oshai.kotlinlogging.KotlinLogging

class ParserEvents : ParseEventListener<Statement>() {
    private val log = KotlinLogging.logger {}

//    override fun afterMatchFailure(event: MatchFailureEvent<Statement>) {
//        super.afterMatchFailure(event)
//        log.debug { "Match failure: ${event}" }
//    }

    override fun afterMatchSuccess(event: MatchSuccessEvent<Statement>) {
        super.afterMatchSuccess(event)
        log.info { "${event.context.matchedInput}" }
    }

//    override fun afterParse(event: PostParseEvent<Statement>) {
//        super.afterParse(event)
//        log.info { "${event.result.}" }
//    }

//    override fun beforeMatch(event: PreMatchEvent<Statement>) {
//        super.beforeMatch(event)
//        log.debug { "Before match: ${event}" }
//    }

    override fun beforeParse(event: PreParseEvent<Statement>) {
        super.beforeParse(event)
        log.info { "${event.context.matchedInput}" }
    }
}