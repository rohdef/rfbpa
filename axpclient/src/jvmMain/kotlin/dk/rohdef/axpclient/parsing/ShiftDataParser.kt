package dk.rohdef.axpclient.parsing

import dk.rohdef.helperplanning.shifts.ShiftData
import org.jsoup.select.Elements

class ShiftDataParser {
    private val shiftParser = ShiftParser()

    fun parse(elements: Elements): ShiftData {
        return if (elements.isEmpty()) {
            ShiftData.NoData
        } else {
            shiftParser.parse(elements)
        }
    }
}