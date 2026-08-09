package dk.rohdef.mithfclient

import com.fleeksoft.ksoup.Ksoup
import dk.rohdef.rfsimplejs.JavaScriptParser

private val jsParser = JavaScriptParser()

fun main() {
    val sample = object {}.javaClass.getResource("/sample.html")!!.readText()

    val ksp = Ksoup.parse(sample)

    val scrpt = ksp
        .body()
        .select("script")
        .first()

    val sbody = scrpt!!.data()

    val e = jsParser.parse(sbody)
    println(e)
}