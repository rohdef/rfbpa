package dk.rohdef.rfsimplejs.ast2.literals

import java.math.BigDecimal
import java.math.BigInteger

sealed interface Number : Literal {
    sealed interface SimpleNumber : Number {
        val number: kotlin.Number

        data class IntegerNumber(override val number: BigInteger) : SimpleNumber

        data class DecimalNumber(override val number: BigDecimal) : SimpleNumber
    }


    data class HexadecimalNumber(val hexadecimal: String) : Number

    data class BinaryNumber(val hexadecimal: String) : Number

    data class ScientificNumber(val coefficient: SimpleNumber, val exponent: SimpleNumber.IntegerNumber) : Number
}