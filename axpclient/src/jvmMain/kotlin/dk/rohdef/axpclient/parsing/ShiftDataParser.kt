package dk.rohdef.axpclient.parsing

import dk.rohdef.axpclient.helper.Shift
import org.jsoup.select.Elements

internal class ShiftDataParser {
    private val shiftParser = ShiftParser()

    fun parse(elements: Elements): List<Shift> {
        return if (elements.isEmpty()) {
            emptyList()
        } else {
            shiftParser.parse(elements)
        }
    }
}
