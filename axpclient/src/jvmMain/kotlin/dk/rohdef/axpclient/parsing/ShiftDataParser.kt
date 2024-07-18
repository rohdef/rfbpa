package dk.rohdef.axpclient.parsing

import dk.rohdef.axpclient.helper.AxpShift
import org.jsoup.select.Elements

internal class ShiftDataParser {
    private val shiftParser = ShiftParser()

    fun parse(elements: Elements): List<AxpShift> {
        return if (elements.isEmpty()) {
            emptyList()
        } else {
            shiftParser.parse(elements)
        }
    }
}
